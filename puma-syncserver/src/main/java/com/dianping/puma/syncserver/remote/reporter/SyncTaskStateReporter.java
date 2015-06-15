package com.dianping.puma.syncserver.remote.reporter;

import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.SyncTaskStateEvent;
import com.dianping.puma.core.service.SyncTaskStateService;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.job.executor.exception.TEException;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("syncTaskStateReporter")
public class SyncTaskStateReporter {

	@Autowired
	SwallowEventPublisher syncTaskStatePublisher;

	@Autowired
	SyncTaskStateService syncTaskStateService;

	@Autowired
	SyncServerConfig syncServerConfig;

	@Autowired
	TaskExecutorContainer defaultTaskExecutorContainer;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		SyncTaskStateEvent event = new SyncTaskStateEvent();
		List<String> serverNames = new ArrayList<String>();
		serverNames.add(syncServerConfig.getSyncServerName());
		event.setServerNames(serverNames);

		Map<String, SyncTaskState> syncTaskStateMap = new HashMap<String, SyncTaskState>();

		List<TaskExecutor> taskExecutors = defaultTaskExecutorContainer.getAll();
		for (TaskExecutor taskExecutor: taskExecutors) {
			if (taskExecutor instanceof SyncTaskExecutor) {
				SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) taskExecutor;
				SyncTaskState syncTaskState = new SyncTaskState();

				// Status.
				syncTaskState.setStatus(syncTaskExecutor.getStatus());

				// BinlogInfo.
				syncTaskState.setBinlogInfo(syncTaskExecutor.getBinlogManager().getBinlogInfo());

				// Exception.
				try {
					syncTaskExecutor.asyncThrow();
					syncTaskState.setException(null);
				} catch (TEException e) {
					syncTaskState.setException(e);
				}

				// Update sql counts.
				syncTaskState.setUpdates(syncTaskExecutor.getUpdates().get());

				// Insert sql counts.
				syncTaskState.setInserts(syncTaskExecutor.getInserts().get());

				// Delete sql counts.
				syncTaskState.setDeletes(syncTaskExecutor.getDeletes().get());

				// Ddl sql counts.
				syncTaskState.setDdls(syncTaskExecutor.getDdls().get());

				syncTaskStateMap.put(syncTaskExecutor.getTask().getName(), syncTaskState);
			}
		}

		event.setSyncTaskStateMap(syncTaskStateMap);
		syncTaskStatePublisher.publish(event);
	}
}
