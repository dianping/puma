package com.dianping.puma.syncserver.remote.reporter.helper;

import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.core.service.SyncTaskStateService;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("syncTaskStateCollector")
public class SyncTaskStateCollector {

	@Autowired
	SyncTaskStateService syncTaskStateService;

	@Autowired
	SyncTaskService syncTaskService;

	@Autowired
	TaskExecutorContainer taskExecutorContainer;

	@Scheduled(cron = "0/5 * * * * ?")
	public void collect() {
		syncTaskStateService.removeAll();

		for (TaskExecutor taskExecutor: taskExecutorContainer.getAll()) {
			if (taskExecutor instanceof SyncTaskExecutor) {
				SyncTaskState syncTaskState = ((SyncTaskExecutor) taskExecutor).getTaskState();
				syncTaskState.setGmtUpdate(new Date());
				syncTaskStateService.add(syncTaskState);
			}
		}
	}
}
