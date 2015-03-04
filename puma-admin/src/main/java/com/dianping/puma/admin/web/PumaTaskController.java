package com.dianping.puma.admin.web;

import com.dianping.puma.admin.reporter.PumaTaskOperationReporter;
import com.dianping.puma.core.container.PumaTaskStateContainer;
import com.dianping.puma.core.model.PumaTaskState;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.SrcDBInstanceService;
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
import java.sql.Timestamp;
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
	PumaTaskStateContainer pumaTaskStateContainer;

	@Autowired
	PumaTaskOperationReporter pumaTaskOperationReporter;

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

	@RequestMapping(value = {
			"/puma-task/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(
			String srcDBInstanceName,
			String pumaServerName,
			String binlogFile,
			Long binlogPosition,
			int preservedDay) {

		Map<String, Object> map = new HashMap<String, Object>();

		PumaTask pumaTask = new PumaTask();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		pumaTask.setName(srcDBInstanceName + "@" + pumaServerName + "-" + timestamp.getTime());
		pumaTask.setSrcDBInstanceName(srcDBInstanceName);
		pumaTask.setPumaServerName(pumaServerName);
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile(binlogFile);
		binlogInfo.setBinlogPosition(binlogPosition);
		pumaTask.setBinlogInfo(binlogInfo);
		pumaTask.setPreservedDay(preservedDay);

		try {
			// Persistent.
			this.pumaTaskService.create(pumaTask);

			// Add puma task state to the state container.
			this.pumaTaskStateContainer.create(pumaTask.getId());

			// Publish puma task operation event to puma server.
			this.pumaTaskOperationReporter.report(pumaServerName, pumaTask.getId(), Operation.CREATE);

			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/puma-task/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			this.pumaTaskService.remove(id);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/puma-task/refresh" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String refreshPost(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			PumaTaskState state = this.pumaTaskStateContainer.get(id);
			if (state == null) {
				throw new Exception("Puma task state not found.");
			}

			map.put("state", state);
			map.put("success", true);
		} catch (Exception e) {
			map.put("err", "world");
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}
}
