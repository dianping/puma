package com.dianping.puma.admin.remote.reporter;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.biz.monitor.SwallowEventPublisher;
import com.dianping.puma.biz.monitor.event.SyncTaskControllerEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("syncTaskControllerReporter")
public class SyncTaskControllerReporter {

	@Autowired
	SwallowEventPublisher syncTaskControllerEventPublisher;

	public void report(String syncServerName, String taskName, ActionController controller) throws SendFailedException {
		SyncTaskControllerEvent event = new SyncTaskControllerEvent();
		event.setServerName(syncServerName);
		event.setTaskName(taskName);
		event.setController(controller);
		syncTaskControllerEventPublisher.publish(event);
	}
}
