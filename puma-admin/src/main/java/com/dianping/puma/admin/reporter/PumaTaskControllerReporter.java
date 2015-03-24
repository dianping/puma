package com.dianping.puma.admin.reporter;

import com.dianping.puma.core.constant.ActionController;
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

	public void report(String pumaServerId, String taskId, String taskName, ActionController controller) throws SendFailedException {
		PumaTaskControllerEvent event = new PumaTaskControllerEvent();
		event.setPumaServerId(pumaServerId);
		event.setTaskId(taskId);
		event.setTaskName(taskName);

		PumaTaskController pumaTaskController = new PumaTaskController();
		pumaTaskController.setController(controller);
		event.setController(pumaTaskController);

		pumaTaskControllerEventPublisher.publish(event);
	}
}
