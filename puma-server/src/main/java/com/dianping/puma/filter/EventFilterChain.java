package com.dianping.puma.filter;

import java.util.List;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;

public interface EventFilterChain {
	public boolean doNext(ChangedEvent event);

	public void setEventFilters(List<EventFilter> eventFilters);

	public void reset();

}