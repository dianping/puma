package com.dianping.puma.admin.remote.reporter;

import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.event.PumaTaskOperationEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pumaTaskOperationReporter")
public class PumaTaskOperationReporter {

	@Autowired
	SwallowEventPublisher pumaTaskOperationEventPublisher;

	public void report(Event event) throws SendFailedException {
		pumaTaskOperationEventPublisher.publish(event);
	}

	public void report(String pumaServerName, String taskName, ActionOperation operation) throws SendFailedException {
		PumaTaskOperationEvent event = new PumaTaskOperationEvent();
		event.setServerName(pumaServerName);
		event.setTaskName(taskName);
		event.setOperation(operation);
		pumaTaskOperationEventPublisher.publish(event);
	}
}
