package com.dianping.puma.core.model.event;

public interface EventListener<T extends Event> {

	void onEvent(T event);
}
