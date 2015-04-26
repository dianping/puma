package com.dianping.puma.core.model.event;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadEventListener implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(DeadEventListener.class);

	@Override
	@Subscribe
	public void onEvent(Event event) {
		LOG.warn("Dead event received: {}.", event.toString());
	}
}
