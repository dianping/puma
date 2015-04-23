package com.dianping.puma.filter;

import com.dianping.puma.core.event.Event;

public interface EventFilter {

	public boolean accept(Event event, EventFilterChain eventfilterChain);

}