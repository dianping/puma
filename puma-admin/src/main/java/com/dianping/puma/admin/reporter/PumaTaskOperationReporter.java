package com.dianping.puma.admin.reporter;

import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.model.PumaTaskOperation;
import com.dianping.puma.core.monitor.PumaTaskOperationEvent;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pumaTaskOperationReporter")
public class PumaTaskOperationReporter {

	@Autowired
	SwallowEventPublisher pumaTaskOperationEventPublisher;

	public void report(String pumaServerName, String taskId, Operation operation) throws SendFailedException {
		PumaTaskOperationEvent event = new PumaTaskOperationEvent();
		event.setPumaServerName(pumaServerName);
		event.setTaskId(taskId);

		PumaTaskOperation pumaTaskOperation = new PumaTaskOperation();
		pumaTaskOperation.setOperation(operation);
		event.setOperation(pumaTaskOperation);

		pumaTaskOperationEventPublisher.publish(event);
	}
}
