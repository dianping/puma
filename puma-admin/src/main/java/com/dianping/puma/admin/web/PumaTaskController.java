package com.dianping.puma.admin.web;

import com.dianping.puma.admin.container.PumaTaskStateContainer;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.entity.PumaServerEntity;
import com.dianping.puma.core.entity.PumaTaskEntity;
import com.dianping.puma.core.entity.SrcDBInstanceEntity;
import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.model.PumaTaskOperation;
import com.dianping.puma.core.monitor.PumaTaskOperationEvent;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
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
	SwallowEventPublisher pumaTaskEventPublisher;

	@RequestMapping(value = { "/puma-task" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<PumaTaskEntity> pumaTaskEntities = pumaTaskService.findAll();

		map.put("entities", pumaTaskEntities);
		map.put("path", "puma-task");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/puma-task/create" }, method = RequestMethod.GET)
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<SrcDBInstanceEntity> srcDBInstanceEntities = srcDBInstanceService.findAll();
		List<PumaServerEntity> pumaServerEntities = pumaServerService.findAll();

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

		PumaTaskEntity pumaTaskEntity = new PumaTaskEntity();
		pumaTaskEntity.setSrcDBInstanceName(srcDBInstanceName);
		pumaTaskEntity.setPumaServerName(pumaServerName);
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile(binlogFile);
		binlogInfo.setBinlogPosition(binlogPosition);
		pumaTaskEntity.setBinlogInfo(binlogInfo);
		pumaTaskEntity.setPreservedDay(preservedDay);

		try {
			// Persistent.
			this.pumaTaskService.create(pumaTaskEntity);

			// Add puma task state to the state container.
			this.pumaTaskStateContainer.create(pumaTaskEntity.getId());

			// Publish puma task operation event to puma server.
			PumaTaskOperationEvent event = new PumaTaskOperationEvent();
			event.setPumaServerName(pumaServerName);
			event.setTaskId(pumaTaskEntity.getId());
			PumaTaskOperation operation = new PumaTaskOperation();
			operation.setOperation(Operation.ADD);
			event.setOperation(operation);
			this.pumaTaskEventPublisher.publish(event);

			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}
}
