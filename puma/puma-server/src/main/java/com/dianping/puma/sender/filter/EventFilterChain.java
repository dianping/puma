package com.dianping.puma.sender.filter;

import java.util.List;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;

public interface EventFilterChain {
	public boolean doNext(ChangedEvent event, PumaContext context);

	public void setEventFilters(List<EventFilter> eventFilters);

}