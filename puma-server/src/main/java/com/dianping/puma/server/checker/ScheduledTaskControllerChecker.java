package com.dianping.puma.server.checker;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.server.server.TaskServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskControllerChecker implements TaskControllerChecker {

	@Autowired
	TaskContainer taskContainer;

	@Autowired
	PumaTaskService pumaTaskService;

	@Autowired
	TaskServerManager taskServerManager;

	@Override
	public void check() {
		for (String host: taskServerManager.findAuthorizedHosts()) {
			for (PumaTaskEntity task: pumaTaskService.findByPumaServerName(host)) {
				switch (task.getActionController()) {
				case START:
					taskContainer.start(task.getName());
					break;
				case STOP:
					taskContainer.stop(task.getName());
					break;
				default:
					break;
				}
			}
		}
	}

	@Scheduled(fixedDelay = 5 * 1000)
	private void scheduledCheck() {
		check();
	}
}
