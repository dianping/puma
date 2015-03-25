package com.dianping.puma.admin.remote.reporter;

import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.SyncTaskOperationEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("syncTaskOperationReporter")
public class SyncTaskOperationReporter {

	@Autowired
	SwallowEventPublisher syncTaskOperationEventPublisher;

	public void report(String syncServerName, String taskName, ActionOperation operation) throws SendFailedException {
		SyncTaskOperationEvent event = new SyncTaskOperationEvent();
		event.setServerName(syncServerName);
		event.setTaskName(taskName);
		event.setOperation(operation);
		syncTaskOperationEventPublisher.publish(event);
	}
}
