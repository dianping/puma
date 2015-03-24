package com.dianping.puma.admin.web;

import com.dianping.puma.admin.remote.reporter.PumaTaskControllerReporter;
import com.dianping.puma.admin.remote.reporter.PumaTaskOperationReporter;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.*;
import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PumaTaskController {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskController.class);

	@Autowired
	PumaTaskService pumaTaskService;

	@Autowired
	SrcDBInstanceService srcDBInstanceService;

	@Autowired
	PumaServerService pumaServerService;

	@Autowired
	TaskStateContainer taskStateContainer;

	@Autowired
	PumaTaskStateService pumaTaskStateService;

	@Autowired
	PumaTaskOperationReporter pumaTaskOperationReporter;

	@Autowired
	PumaTaskControllerReporter pumaTaskControllerReporter;

	@Autowired
	SyncTaskService syncTaskService;

	@RequestMapping(value = { "/puma-task" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<PumaTask> pumaTaskEntities = pumaTaskService.findAll();

		map.put("entities", pumaTaskEntities);
		map.put("path", "puma-task");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/puma-task/create" }, method = RequestMethod.GET)
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<SrcDBInstance> srcDBInstanceEntities = srcDBInstanceService.findAll();
		List<PumaServer> pumaServerEntities = pumaServerService.findAll();

		map.put("srcDBInstanceEntities", srcDBInstanceEntities);
		map.put("pumaServerEntities", pumaServerEntities);
		map.put("path", "puma-task");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/puma-task/update" }, method = RequestMethod.GET)
	public ModelAndView update(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<SrcDBInstance> srcDBInstanceEntities = srcDBInstanceService.findAll();
			List<PumaServer> pumaServerEntities = pumaServerService.findAll();

			map.put("srcDBInstanceEntities", srcDBInstanceEntities);
			map.put("pumaServerEntities", pumaServerEntities);

			PumaTask pumaTask = pumaTaskService.find(id);

			map.put("entity", pumaTask);
			map.put("path", "puma-task");
			map.put("subPath", "create");
		} catch (Exception e) {
			// @TODO: error page.
		}

		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = {
			"/puma-task/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(
			String name,
			String srcDBInstanceName,
			String pumaServerName,
			String binlogFile,
			Long binlogPosition,
			int preservedDay) {

		Map<String, Object> map = new HashMap<String, Object>();

		ActionOperation operation;

		try {
			PumaTask pumaTask;

			// Create or update?
			if (name != null) {
				// Update.
				pumaTask = pumaTaskService.find(name);

				if (!binlogFile.equals(pumaTask.getBinlogInfo().getBinlogFile())
						|| !binlogPosition.equals(pumaTask.getBinlogInfo().getBinlogPosition())) {
					operation = ActionOperation.UPDATE;
				} else {
					operation = ActionOperation.PROLONG;
				}

			} else {
				// Create.
				operation = ActionOperation.CREATE;
				pumaTask = new PumaTask();
			}


			SrcDBInstance srcDBInstance = srcDBInstanceService.find(srcDBInstanceName);
			PumaServer pumaServer = pumaServerService.find(pumaServerName);

			pumaTask.setName(name);
			pumaTask.setSrcDBInstanceId(srcDBInstance.getName());
			pumaTask.setPumaServerId(pumaServer.getName());
			BinlogInfo binlogInfo = new BinlogInfo();
			binlogInfo.setBinlogFile(binlogFile);
			binlogInfo.setBinlogPosition(binlogPosition);
			pumaTask.setBinlogInfo(binlogInfo);
			pumaTask.setPreservedDay(preservedDay);
			pumaTask.setSrcDBInstanceName(srcDBInstance.getName());
			pumaTask.setPumaServerName(pumaServer.getName());

			if (name != null) {
				this.pumaTaskService.update(pumaTask);
			} else {
				this.pumaTaskService.create(pumaTask);
			}

			// Add puma task state to the state container.
			PumaTaskState taskState = new PumaTaskState();
			taskState.setTaskName(pumaTask.getName());
			taskState.setStatus(Status.PREPARING);
			pumaTaskStateService.add(taskState);

			// Publish puma task operation event to puma server.
			this.pumaTaskOperationReporter.report(pumaServer.getName(), pumaTask.getName(), operation);

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

	@RequestMapping(value = { "/puma-task/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			PumaTask pumaTask = pumaTaskService.find(name);

			this.pumaTaskService.remove(name);

			pumaTaskStateService.remove(name);

			// Publish puma task operation event to puma server.
			this.pumaTaskOperationReporter.report(pumaTask.getPumaServerId(), pumaTask.getId(), pumaTask.getName(), ActionOperation.REMOVE);

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

	@RequestMapping(value = { "/puma-task/refresh" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String refreshPost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			PumaTaskState taskState = pumaTaskStateService.find(name);

			if (taskState == null) {
				throw new Exception("Puma task state not found.");
			}

			map.put("state", taskState);
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

	@RequestMapping(value = { "/puma-task/resume" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String resumePost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			PumaTask pumaTask = pumaTaskService.find(name);

			// Publish puma task controller event to puma server.
			this.pumaTaskControllerReporter.report(pumaTask.getPumaServerId(), pumaTask.getId(), pumaTask.getName(), com.dianping.puma.core.constant.ActionController.RESUME);

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

	@RequestMapping(value = { "/puma-task/pause" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String pausePost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			PumaTask pumaTask = pumaTaskService.find(name);

			// Publish puma task controller event to puma server.
			this.pumaTaskControllerReporter.report(pumaTask.getPumaServerId(), pumaTask.getId(), pumaTask.getName(), com.dianping.puma.core.constant.ActionController.PAUSE);

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
