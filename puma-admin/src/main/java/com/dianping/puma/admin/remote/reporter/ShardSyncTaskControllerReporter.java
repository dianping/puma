package com.dianping.puma.admin.remote.reporter;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.ShardSyncTaskControllerEvent;
import com.dianping.puma.core.monitor.event.SyncTaskOperationEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("shardSyncTaskControllerReporter")
public class ShardSyncTaskControllerReporter {

	@Autowired
	SwallowEventPublisher shardSyncTaskControllerEventPublisher;

	public void report(String syncServerName, String taskName, ActionController operation) throws SendFailedException {
		ShardSyncTaskControllerEvent event = new ShardSyncTaskControllerEvent();
		event.setServerName(syncServerName);
		event.setTaskName(taskName);
		event.setController(operation);
		shardSyncTaskControllerEventPublisher.publish(event);
	}
}
