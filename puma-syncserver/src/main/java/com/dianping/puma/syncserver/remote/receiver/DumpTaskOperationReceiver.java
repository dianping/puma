package com.dianping.puma.syncserver.remote.receiver;

import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.entity.DumpTask;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.event.DumpTaskOperationEvent;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.service.DumpTaskService;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.job.executor.builder.TaskExecutorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dumpTaskOperationReceiver")
public class DumpTaskOperationReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(DumpTaskOperationReceiver.class);

	@Autowired
	DumpTaskService dumpTaskService;

	@Autowired
	TaskExecutorBuilder taskExecutorBuilder;

	@Autowired
	TaskExecutorContainer taskExecutorContainer;

	@Autowired
	NotifyService notifyService;

	@Override
	public void onEvent(Event event) {
		if (event instanceof DumpTaskOperationEvent) {
			LOG.info("Receive dump task operation event.");

			String taskName = ((DumpTaskOperationEvent) event).getTaskName();
			ActionOperation operation = ((DumpTaskOperationEvent) event).getOperation();

			switch (operation) {
			case CREATE:
				DumpTask syncTask = dumpTaskService.find(taskName);
				TaskExecutor taskExecutor = taskExecutorBuilder.build(syncTask);

				try {
					taskExecutorContainer.submit(taskExecutor);
				} catch (TaskExecutionException e) {
					notifyService.alarm(e.getMessage(), e, false);
				}
				break;

			case REMOVE:
				taskExecutorContainer.deleteSyncTask(taskName);
			}
		}
	}

}
