package com.dianping.puma.syncserver.remote.receiver;

import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.event.SyncTaskControllerEvent;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("shardSyncTaskControllerReceiver")
public class ShardSyncTaskControllerReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(ShardSyncTaskControllerReceiver.class);

	@Autowired
	TaskExecutorContainer taskExecutorContainer;

	@Override
	public void onEvent(Event event) {
		if (event instanceof SyncTaskControllerEvent) {
			LOG.info("Receive sync task controller event.");

//			SyncTaskControllerEvent syncTaskControllerEvent = (SyncTaskControllerEvent) event;
//			taskExecutorContainer
//					.changeStatus(syncTaskControllerEvent.getTaskName(), syncTaskControllerEvent.getController());
		}
	}
}
