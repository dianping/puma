package com.dianping.puma.syncserver.remote.receiver;

import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.SyncTaskControllerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("syncTaskControllerReceiver")
public class SyncTaskControllerReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskControllerEvent.class);

	@Override
	public void onEvent(Event event) {
		if (event instanceof SyncTaskControllerEvent) {
			LOG.info("Receive sync task controller event.");


		} else {
			LOG.warn("Receive illegal sync task controller event: {}.", event);

			// @TODO
			// Log event exception into CAT.
		}
	}
}
