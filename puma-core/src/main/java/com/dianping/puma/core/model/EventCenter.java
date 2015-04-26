package com.dianping.puma.core.model;

import com.dianping.puma.core.model.event.DeadEventListener;
import com.google.common.eventbus.EventBus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("eventCenter")
public class EventCenter {

	private EventBus eventBus;

	@PostConstruct
	public void init() {
		eventBus = new EventBus();
		eventBus.register(new DeadEventListener());
	}

	public void register(Object eventListener) {
		eventBus.register(eventListener);
	}

	public void post(Object event) {
		eventBus.post(event);
	}
}
