package com.dianping.puma.admin.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dianping.puma.admin.config.Config;
import com.dianping.puma.admin.model.SyncTaskDto;
import com.dianping.puma.admin.model.mapper.ErrorListMapper;
import com.dianping.puma.admin.model.mapper.SyncTaskMapper;
import com.dianping.puma.admin.remote.reporter.SyncTaskControllerReporter;
import com.dianping.puma.admin.remote.reporter.SyncTaskOperationReporter;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.state.BaseSyncTaskState;
import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.DstDBInstanceService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SyncServerService;
import com.dianping.puma.core.service.SyncTaskStateService;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.mongodb.MongoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SyncServer;
import com.dianping.puma.core.entity.SyncTask;

@Controller
public class SyncTaskController {

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskController.class);

	@Autowired
	private SyncTaskService syncTaskService;

	@Autowired
	private PumaTaskService pumaTaskService;

	@Autowired
	private DstDBInstanceService dstDBInstanceService;

	@Autowired
	private SyncServerService syncServerService;

	@Autowired
	SyncTaskStateService syncTaskStateService;

	@Autowired
	private TaskStateContainer syncTaskStateContainer;

	@Autowired
	private SyncTaskOperationReporter syncTaskOperationReporter;

	@Autowired
	private SyncTaskControllerReporter syncTaskControllerReporter;

	private static final int PAGESIZE = 30;

	@RequestMapping(value = { "/sync-task" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "sync-task");
		return new ModelAndView("common/main-container", map);
	}

	@RequestMapping(value = { "/sync-task/{pageNum}" })
	public ModelAndView created0(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("pageNum") Integer pageNum) {
		Map<String, Object> map = new HashMap<String, Object>();
		int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
		List<SyncTask> syncTasks = syncTaskService.find(offset, PAGESIZE);
		map.put("syncTasks", syncTasks);
		map.put("createdActive", "active");
		map.put("subPath", "main");
		map.put("path", "sync-task");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/sync-task/list" }, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String list(int page, int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();
		long count = syncTaskService.count();
		List<SyncTask> syncTasks = syncTaskService.findByPage(page, pageSize);
		List<SyncTaskState> syncTaskStates = syncTaskStateService.findAll();
		map.put("count", count);
		map.put("list", syncTasks);
		map.put("state", syncTaskStates);
		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/sync-task/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			// Actually name here.

			SyncTask syncTask = syncTaskService.find(name);

			syncTaskService.remove(name);
			// Publish puma task operation event to puma server.
			syncTaskOperationReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionOperation.REMOVE);

			map.put("success", true);
		} catch (MongoException e) {
			map.put("error", "storage");
			map.put("success", false);
		} catch (SendFailedException e) {
			map.put("error", "notify");
			map.put("success", false);
		} catch (Exception e) {
			map.put("error", e.getMessage());
			map.put("success", false);
		}

		map.put("success", true);

		return GsonUtil.toJson(map);
	}

	/**
	 * 查询SyncTask状态
	 */
	@RequestMapping(value = "/sync-task/status", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object status(HttpSession session, String taskName) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			SyncTaskState syncTaskState = syncTaskStateService.find(taskName);
			// binlog信息，从数据库查询binlog位置即可，不需要从SyncServer实时发过来的status中的binlog获取
			// binlogInfoOfIOThread则是从status中获取
			SyncTask syncTask = this.syncTaskService.find(taskName);
			syncTaskState.setBinlogInfo(syncTask.getBinlogInfo());

			map.put("status", syncTaskState);
			map.put("success", true);
		} catch (IllegalArgumentException e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
		} catch (Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
			LOG.error(e.getMessage(), e);
		}
		return GsonUtil.toJson(map);
	}

	/**
	 * 查询SyncTask
	 */
	@RequestMapping(value = "/sync-task/detail/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public ModelAndView task(HttpSession session, @PathVariable long id) {
		Map<String, Object> map = new HashMap<String, Object>();
		SyncTask syncTask = this.syncTaskService.find(id);
		if (syncTask != null) {
			SyncTaskState syncTaskState = syncTaskStateService.find(syncTask.getName());
			map.put("syncTask", syncTask);
			map.put("syncTaskState", syncTaskState);
			map.put("createdActive", "active");
			map.put("subPath", "detail");
			map.put("path", "sync-task");
		}
		return new ModelAndView("main/container", map);
	}

	/**
	 * 暂停SyncTask状态
	 */
	@RequestMapping(value = "/sync-task/pause", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object pause(HttpSession session, String name) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			SyncTask syncTask = syncTaskService.find(name);
			SyncTaskState syncTaskState = syncTaskStateService.find(syncTask.getName());
			syncTaskState.setStatus(Status.STOPPING);
			this.syncTaskService.updateStatusAction(name, ActionController.PAUSE);
			syncTaskControllerReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionController.PAUSE);
			map.put("success", true);
		} catch (IllegalArgumentException e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
		} catch (Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
			LOG.error(e.getMessage(), e);
		}

		return GsonUtil.toJson(map);
	}

	/**
	 * 恢复SyncTask的运行
	 */
	@RequestMapping(value = "/sync-task/resume", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object rerun(HttpSession session, String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			SyncTask syncTask = syncTaskService.find(name);
			SyncTaskState syncTaskState = syncTaskStateService.find(syncTask.getName());
			syncTaskState.setStatus(Status.PREPARING);
			this.syncTaskService.updateStatusAction(name, ActionController.RESUME);
			syncTaskControllerReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionController.RESUME);
			map.put("success", true);
		} catch (IllegalArgumentException e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
		} catch (Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
			LOG.error(e.getMessage(), e);
		}
		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/sync-task/create" }, method = RequestMethod.GET)
	public ModelAndView create(HttpSession session) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 查询MysqlConfig
		map.put("id", 0);
		map.put("path", "sync-task");
		map.put("subPath", "create");
		return new ModelAndView("common/main-container", map);
	}

	@RequestMapping(value = { "/sync-task/update/{id}" }, method = RequestMethod.GET)
	public ModelAndView update(@PathVariable long id) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 查询MysqlConfig
		map.put("id", id);
		map.put("path", "sync-task");
		map.put("subPath", "create");
		return new ModelAndView("common/main-container", map);
	}

	@RequestMapping(value = { "/sync-task/find/{id}" }, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String find(@PathVariable long id) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<PumaTask> pumaTasks = pumaTaskService.findAll();
		List<DstDBInstance> dstDBInstances = dstDBInstanceService.findAll();
		List<SyncServer> syncServers = syncServerService.findAll();

		map.put("pumaTasks", pumaTasks);
		map.put("dstDBInstances", dstDBInstances);
		map.put("syncServers", syncServers);
		map.put("errorSet", ErrorListMapper.convertToErrorSetDto(Config.getInstance().getErrorCodeHandlerMap()));
		SyncTask syncTask = null;
		if (id > 0) {
			syncTask = syncTaskService.find(id);
		}
		map.put("entity", SyncTaskMapper.convertToSyncTaskDto(syncTask));
		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/sync-task/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(@RequestBody SyncTaskDto syncTaskDto) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			SyncTask syncTask = syncTaskService.find(syncTaskDto.getName());
			if (syncTask != null) {
				throw new Exception("duplicate name.");
			}
			syncTask = SyncTaskMapper.convertToSyncTask(syncTaskDto);
			syncTask.setController(ActionController.START);
			syncTaskService.create(syncTask);
			BaseSyncTaskState state = new BaseSyncTaskState();
			state.setStatus(Status.PREPARING);
			syncTaskStateContainer.add(syncTask.getName(), state);
			syncTaskOperationReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionOperation.CREATE);
			map.put("success", true);
		} catch (IllegalArgumentException e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
		} catch (Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
			LOG.error(e.getMessage(), e);
		}
		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/sync-task/update/{id}" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String updatePost(@PathVariable long id, @RequestBody SyncTaskDto syncTaskDto) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			SyncTask syncTask = syncTaskService.find(id);
			ActionOperation operation = null;
			if (syncTask == null) {
				operation = ActionOperation.CREATE;
			} else {
				operation = ActionOperation.UPDATE;
			}
			syncTask = SyncTaskMapper.convertToSyncTask(syncTaskDto);
			if (operation == ActionOperation.CREATE) {
				syncTaskService.create(syncTask);
			} else {
				syncTask.setId(id);
				syncTaskService.update(syncTask);
			}
			BaseSyncTaskState state = new BaseSyncTaskState();
			state.setStatus(Status.PREPARING);
			syncTaskStateContainer.add(syncTask.getName(), state);
			syncTaskOperationReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionOperation.UPDATE);
			map.put("success", true);
		} catch (IllegalArgumentException e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
		} catch (Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
			LOG.error(e.getMessage(), e);
		}
		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/sync-task/refresh" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String refreshPost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			SyncTaskState syncTaskState = syncTaskStateService.find(name);
			if (syncTaskState == null) {
				throw new Exception("Sync task state not found.");
			}
			if ((new Date()).getTime() - syncTaskState.getGmtUpdate().getTime() > 60 * 1000) {
				syncTaskState.setStatus(Status.DISCONNECTED);
			}
			map.put("state", syncTaskState);
			map.put("success", true);
		} catch (MongoException e) {
			map.put("error", "storage");
			map.put("success", false);
		} catch (Exception e) {
			map.put("error", e.getMessage());
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}
}
