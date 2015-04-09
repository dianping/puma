package com.dianping.puma.admin.web;

import com.dianping.puma.admin.remote.reporter.PumaTaskControllerReporter;
import com.dianping.puma.admin.remote.reporter.PumaTaskOperationReporter;
import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.*;
import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.model.AcceptedTables;
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
import org.springframework.web.bind.annotation.PathVariable;
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

	private static final int String = 0;

	private static final Class<Void> AcceptedDataInfo = null;

	private static final int Map = 0;

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
	public ModelAndView view() {
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

	@RequestMapping(value = { "/puma-task/update/{id}" }, method = RequestMethod.GET)
	public ModelAndView update(@PathVariable long id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<SrcDBInstance> srcDBInstanceEntities = srcDBInstanceService.findAll();
			List<PumaServer> pumaServerEntities = pumaServerService.findAll();

			map.put("srcDBInstanceEntities", srcDBInstanceEntities);
			map.put("pumaServerEntities", pumaServerEntities);

			PumaTask pumaTask = pumaTaskService.find(id);

			map.put("entity", pumaTask);
			if(pumaTask.getAcceptedDataInfos()!=null){
				map.put("acceptedDataInfoStr", GsonUtil.toJson(pumaTask.getAcceptedDataInfos()));
			}
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
			int preservedDay,
			String acceptedDataInfoStr) {

		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionOperation operation;

			PumaTask pumaTask = pumaTaskService.find(name);
			if (pumaTask == null) {
				pumaTask = new PumaTask();
				operation = ActionOperation.CREATE;
			} else {
				if (!binlogFile.equals(pumaTask.getBinlogInfo().getBinlogFile())
						|| !binlogPosition.equals(pumaTask.getBinlogInfo().getBinlogPosition())) {
					operation = ActionOperation.UPDATE;
				} else if(pumaTask.getPreservedDay()!= preservedDay){
					operation = ActionOperation.PROLONG;
				}else{
					operation = ActionOperation.FILTER;
				}
			}

			pumaTask.setName(name);
			pumaTask.setSrcDBInstanceName(srcDBInstanceName);
			pumaTask.setPumaServerName(pumaServerName);
			BinlogInfo binlogInfo = new BinlogInfo();
			binlogInfo.setBinlogFile(binlogFile);
			binlogInfo.setBinlogPosition(binlogPosition);
			pumaTask.setBinlogInfo(binlogInfo);
			pumaTask.setPreservedDay(preservedDay);
			Map<String,AcceptedTables> acceptedDataInfoMap = GsonUtil.fromJson(acceptedDataInfoStr, Map.class);
			pumaTask.setAcceptedDataInfos(acceptedDataInfoMap);
			//pumaTask.setAcceptedDataInfos(acceptedDataInfos)
			// Save puma task state to persistent storage.
			if (operation == ActionOperation.CREATE) {
				pumaTaskService.create(pumaTask);
			} else {
				pumaTaskService.update(pumaTask);
			}

			// Add puma task state to the state container.
			PumaTaskState taskState = new PumaTaskState();
			taskState.setTaskName(pumaTask.getName());
			taskState.setStatus(Status.PREPARING);
			pumaTaskStateService.add(taskState);

			// Publish puma task operation event to puma server.
			this.pumaTaskOperationReporter.report(pumaServerName, name, operation);

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
			this.pumaTaskOperationReporter.report(pumaTask.getPumaServerName(), pumaTask.getName(), ActionOperation.REMOVE);

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

			PumaTaskState taskState = pumaTaskStateService.find(name);
			taskState.setStatus(Status.PREPARING);

			// Publish puma task controller event to puma server.
			this.pumaTaskControllerReporter.report(pumaTask.getPumaServerName(), pumaTask.getName(), ActionController.RESUME);

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

			PumaTaskState taskState = pumaTaskStateService.find(name);
			taskState.setStatus(Status.STOPPING);

			pumaTaskControllerReporter.report(pumaTask.getPumaServerName(), pumaTask.getName(), com.dianping.puma.core.constant.ActionController.PAUSE);

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
