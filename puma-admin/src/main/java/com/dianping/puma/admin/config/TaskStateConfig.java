package com.dianping.puma.admin.config;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.PumaTaskStateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TaskStateConfig {

	@Autowired
	TaskStateContainer taskStateContainer;

	@Autowired
	PumaTaskService pumaTaskService;

	@Autowired
	PumaTaskStateService pumaTaskStateService;

	@PostConstruct
	public void init() {
		List<PumaTask> pumaTasks = pumaTaskService.findAll();

		for (PumaTask pumaTask : pumaTasks) {
			if (pumaTask.getPumaServerNames() != null) {
				for (String serverName : pumaTask.getPumaServerNames()) {

					PumaTaskState taskState = new PumaTaskState();
					taskState.setName(pumaTaskStateService.getTaskStateName(pumaTask.getName(), serverName));
					taskState.setServerName(pumaTask.getPumaServerName());
					taskState.setTaskName(pumaTask.getName());
					taskState.setStatus(Status.PREPARING);
					taskStateContainer.add(taskState.getName(), taskState);
				}
			} else {
				PumaTaskState taskState = new PumaTaskState();
				taskState.setName(pumaTaskStateService.getTaskStateName(pumaTask.getName(), pumaTask
						.getPumaServerName()));
				taskState.setServerName(pumaTask.getPumaServerName());
				taskState.setTaskName(pumaTask.getName());
				taskState.setStatus(Status.PREPARING);
				taskStateContainer.add(taskState.getName(), taskState);
			}
		}
	}
}
