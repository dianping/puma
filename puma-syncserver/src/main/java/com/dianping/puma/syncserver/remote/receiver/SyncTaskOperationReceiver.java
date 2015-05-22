package com.dianping.puma.syncserver.remote.receiver;

import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.SyncTaskOperationEvent;
import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.job.executor.builder.TaskExecutorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("syncTaskOperationReceiver")
public class SyncTaskOperationReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskOperationReceiver.class);

	@Autowired
	SyncTaskService syncTaskService;

	@Autowired
	TaskExecutorBuilder taskExecutorBuilder;

	@Autowired
	TaskExecutorContainer taskExecutorContainer;

	@Override
	public void onEvent(Event event) {
		if (event instanceof SyncTaskOperationEvent) {
			LOG.info("Receive sync task operation event({}).", event.toString());

			String name = ((SyncTaskOperationEvent) event).getTaskName();
			ActionOperation operation = ((SyncTaskOperationEvent) event).getOperation();

			switch (operation) {
			case CREATE:
				createSyncTask(name);
				break;
			case UPDATE:
				updateSyncTask(name);
				break;
			case REMOVE:
				removeSyncTask(name);
				break;
			}
		}
	}

	private void createSyncTask(String name) {
		LOG.info("Creating sync task({})...", name);

		SyncTask syncTask = syncTaskService.find(name);
		TaskExecutor taskExecutor = taskExecutorBuilder.build(syncTask);

		taskExecutorContainer.submit(name, taskExecutor);

		taskExecutorContainer.start(name);
	}

	private void updateSyncTask(String name) {
		LOG.info("Updating sync task({})...", name);

		// Withdraw the original task.
		taskExecutorContainer.withdraw(name);

		// Submit the new task.
		SyncTask syncTask = syncTaskService.find(name);
		TaskExecutor taskExecutor = taskExecutorBuilder.build(syncTask);
		taskExecutorContainer.submit(name, taskExecutor);
	}

	private void removeSyncTask(String name) {
		LOG.info("Removing sync task({})...", name);

		taskExecutorContainer.die(name);

		// Withdraw the original task.
		taskExecutorContainer.withdraw(name);
	}
}
