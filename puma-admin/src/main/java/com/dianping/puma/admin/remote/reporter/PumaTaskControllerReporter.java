package com.dianping.puma.admin.remote.reporter;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.PumaTaskControllerEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pumaTaskControllerReporter")
public class PumaTaskControllerReporter {

	@Autowired
	SwallowEventPublisher pumaTaskControllerEventPublisher;


	public void report(String pumaServerName, String taskName, ActionController controller) throws SendFailedException {
		PumaTaskControllerEvent event = new PumaTaskControllerEvent();
		event.setServerName(pumaServerName);
		event.setTaskName(taskName);
		event.setController(controller);
		pumaTaskControllerEventPublisher.publish(event);
	}
}
