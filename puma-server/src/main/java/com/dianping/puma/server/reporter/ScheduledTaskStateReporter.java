package com.dianping.puma.server.reporter;

import com.dianping.puma.biz.service.PumaTaskStateService;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.taskexecutor.TaskExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskStateReporter implements TaskStateReporter {

	@Autowired
	TaskContainer taskContainer;

	@Autowired
	PumaTaskStateService pumaTaskStateService;

	@Override
	public void report() {
		for (TaskExecutor taskExecutor: taskContainer.getAll()) {
			pumaTaskStateService.createOrUpdate(taskExecutor.getTaskState());
		}
	}

	@Scheduled(fixedDelay = 1000)
	private void scheduledReport() {
		report();
	}
}
