package com.dianping.puma.admin.reporter;

import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.SyncTaskOperationEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("syncTaskOperationReporter")
public class SyncTaskOperationReporter {

	@Autowired
	SwallowEventPublisher syncTaskOperationEventPublisher;

	public void report(String syncServerName, SyncType syncType, String taskName, Operation operation) throws
			SendFailedException {
		SyncTaskOperationEvent event = new SyncTaskOperationEvent();
		event.setSyncServerName(syncServerName);
		event.setSyncType(syncType);
		event.setTaskName(taskName);
		event.setOperation(operation);

		syncTaskOperationEventPublisher.publish(event);
	}
}
