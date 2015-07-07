package com.dianping.puma.remote.receiver;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.biz.event.entity.Event;
import com.dianping.puma.biz.event.EventListener;
import com.dianping.puma.biz.event.entity.PumaTaskControllerEvent;
import com.dianping.puma.server.TaskExecutorContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pumaTaskControllerChecker")
public class PumaTaskControllerChecker implements EventListener {

	public static final Logger LOG = LoggerFactory.getLogger(PumaTaskControllerChecker.class);

	@Autowired
	private TaskExecutorContainer taskExecutorContainer;

	@Override
	public void onEvent(Event event) {
		LOG.info("Receive puma task event!");

		if (event instanceof PumaTaskControllerEvent) {
			LOG.info("Receive puma task controller event.");

			PumaTaskControllerEvent pumaTaskControllerEvent = (PumaTaskControllerEvent) event;
			ActionController controller = pumaTaskControllerEvent.getController();

			switch (controller) {
			case PAUSE:
				LOG.info("Receive puma task controller event: PAUSE.");
				taskExecutorContainer.pauseEvent(pumaTaskControllerEvent);
				break;
			case RESUME:
				LOG.info("Receive puma task controller event: RESUME.");
				taskExecutorContainer.resumeEvent(pumaTaskControllerEvent);
				break;
			default:
				LOG.error("Receive illegal puma task controller event `{}`.", controller);
			}
		} else {
			LOG.error("Receive illegal puma task event `{}`.", event);
		}
	}
}
