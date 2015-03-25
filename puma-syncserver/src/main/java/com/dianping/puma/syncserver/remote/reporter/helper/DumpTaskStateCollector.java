package com.dianping.puma.syncserver.remote.reporter.helper;

import com.dianping.puma.core.model.state.DumpTaskState;
import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.core.service.DumpTaskService;
import com.dianping.puma.core.service.DumpTaskStateService;
import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.core.service.SyncTaskStateService;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("dumpTaskStateCollector")
public class DumpTaskStateCollector {

	@Autowired
	DumpTaskStateService dumpTaskStateService;

	@Autowired
	DumpTaskService dumpTaskService;

	@Autowired
	TaskExecutorContainer taskExecutorContainer;

	@Scheduled(cron = "0/5 * * * * ?")
	public void collect() {
		dumpTaskStateService.removeAll();

		for (TaskExecutor taskExecutor: taskExecutorContainer.getAll()) {
			if (taskExecutor instanceof DumpTaskExecutor) {
				DumpTaskState dumpTaskState = ((DumpTaskExecutor) taskExecutor).getTaskState();
				dumpTaskState.setGmtUpdate(new Date());
				dumpTaskStateService.add(dumpTaskState);
			}
		}
	}
}