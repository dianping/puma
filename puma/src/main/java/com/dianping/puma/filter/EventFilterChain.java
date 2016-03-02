package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;

import java.util.List;

public interface EventFilterChain {
	boolean doNext(ChangedEvent event);

	void setEventFilters(List<EventFilter> eventFilters);

	void reset();

}