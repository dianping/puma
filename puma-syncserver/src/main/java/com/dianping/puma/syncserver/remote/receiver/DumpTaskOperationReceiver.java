package com.dianping.puma.syncserver.remote.receiver;

import com.dianping.puma.biz.monitor.EventListener;
import com.dianping.puma.biz.monitor.NotifyService;
import com.dianping.puma.biz.monitor.event.Event;
import com.dianping.puma.biz.service.DumpTaskService;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
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
		/*
		if (event instanceof DumpTaskOperationEvent) {
			LOG.info("Receive dump task operation event.");

			String taskName = ((DumpTaskOperationEvent) event).getTaskName();
			ActionOperation operation = ((DumpTaskOperationEvent) event).getOperation();

			switch (operation) {
			case CREATE:
				DumpTask syncTask = dumpTaskService.find(taskName);
				TaskExecutor taskExecutor = taskExecutorBuilder.build(syncTask);

				try {
					taskExecutorContainer.submit(taskName, taskExecutor);
				} catch (TECException e) {
					notifyService.alarm(e.getMessage(), e, false);
				}
				break;

			case REMOVE:
				taskExecutorContainer.withdraw(taskName);
			}
		}*/
	}
}
