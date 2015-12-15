package com.dianping.puma.filter;

import java.util.List;

import com.dianping.puma.core.event.ChangedEvent;

public interface EventFilterChain {
	boolean doNext(ChangedEvent event);

	void setEventFilters(List<EventFilter> eventFilters);

	void reset();

}