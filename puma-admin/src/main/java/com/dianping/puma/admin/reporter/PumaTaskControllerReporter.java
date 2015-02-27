package com.dianping.puma.admin.reporter;

import com.dianping.puma.core.constant.Controller;
import com.dianping.puma.core.model.PumaTaskController;
import com.dianping.puma.core.monitor.PumaTaskControllerEvent;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pumaTaskControllerReporter")
public class PumaTaskControllerReporter {

	@Autowired
	SwallowEventPublisher pumaTaskControllerEventPublisher;

	public void report(String pumaServerName, String taskId, Controller controller) throws SendFailedException {
		PumaTaskControllerEvent event = new PumaTaskControllerEvent();
		event.setPumaServerName(pumaServerName);
		event.setTaskId(taskId);

		PumaTaskController pumaTaskOperation = new PumaTaskController();
		pumaTaskOperation.setController(controller);
		event.setController(pumaTaskOperation);

		pumaTaskControllerEventPublisher.publish(event);
	}
}
