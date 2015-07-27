package com.dianping.puma.server.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.taskexecutor.TaskExecutor;

@Controller
@RequestMapping(value = "/status")
public class StatusController {

	@Autowired
	TaskContainer taskContainer;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> index() {
		Map<String, Object> status = new HashMap<String, Object>();

		status.put("status", SystemStatusManager.status);

		return status;
	}

	@RequestMapping(value = "puma-task", method = RequestMethod.GET)
	@ResponseBody
	public List<PumaTaskStateEntity> pumaTask() {
		List<PumaTaskStateEntity> pumaTaskStates = new ArrayList<PumaTaskStateEntity>();

		for (TaskExecutor taskExecutor : taskContainer.getAll()) {
			PumaTaskStateEntity taskState = taskExecutor.getTaskState();
			// taskState.setServerName(pumaServerConfig.getName());
			taskState.setTaskName(taskState.getTaskName());
			taskState.setUpdateTime(new Date());
			pumaTaskStates.add(taskState);
		}
		
		return pumaTaskStates;
	}
}
