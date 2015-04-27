package com.dianping.puma.core.model.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("eventCenter")
public class EventCenter {

	private static final Logger LOG = LoggerFactory.getLogger(EventCenter.class);

	private EventBus eventBus = new EventBus();

	@PostConstruct
	public void init() {
		eventBus.register(new DeadEventListener());
	}

	public void register(Object eventListener) {
		eventBus.register(eventListener);
	}

	public void post(Object event) {
		eventBus.post(event);
	}

	private class DeadEventListener {

		@Subscribe
		public void onEvent(DeadEvent event) {
			LOG.warn("Dead event received: {}.", event.toString());
		}
	}
}
