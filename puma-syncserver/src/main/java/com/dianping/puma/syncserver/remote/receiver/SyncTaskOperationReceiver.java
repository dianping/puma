package com.dianping.puma.syncserver.remote.receiver;

import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.SyncTaskOperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncTaskOperationReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskOperationReceiver.class);

	@Override
	public void onEvent(Event event) {
		if (event instanceof SyncTaskOperationEvent) {
			LOG.info("Receive sync task operation event.");
		} else {
			LOG.warn("Receive illegal sync task operation event.");

			// @TODO
			// Log event exception into CAT.
		}
	}
}
