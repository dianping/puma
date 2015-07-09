package com.dianping.puma.config;

import com.dianping.puma.biz.entity.PumaTaskState;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.biz.entity.old.PumaTask;
import com.dianping.puma.biz.service.PumaTaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class PumaTaskStateConfig {

	@Autowired
	TaskStateContainer taskStateContainer;

	@Autowired
	PumaTaskService pumaTaskService;

	@Autowired
	PumaServerConfig pumaServerConfig;

	@Autowired
	PumaTaskStateService pumaTaskStateService;

	@PostConstruct
	public void init() {
		List<PumaTask> pumaTasks = pumaTaskService.findByPumaServerName(pumaServerConfig.getName());

		for (PumaTask pumaTask : pumaTasks) {
			if (pumaTask.getPumaServerNames() != null) {
				for (String serverName : pumaTask.getPumaServerNames()) {

					PumaTaskState taskState = new PumaTaskState();
					taskState.setName(pumaTaskStateService.getStateName(pumaTask.getName(), serverName));
					taskState.setServerName(pumaTask.getPumaServerName());
					taskState.setTaskName(pumaTask.getName());
					taskState.setStatus(Status.PREPARING);
					taskStateContainer.add(taskState.getName(), taskState);
				}
			} else {
				PumaTaskState taskState = new PumaTaskState();
				taskState.setName(pumaTaskStateService.getStateName(pumaTask.getName(), pumaTask
						.getPumaServerName()));
				taskState.setServerName(pumaTask.getPumaServerName());
				taskState.setTaskName(pumaTask.getName());
				taskState.setStatus(Status.PREPARING);
				taskStateContainer.add(taskState.getName(), taskState);
			}
		}
	}
}
