package com.dianping.puma.sender.filter;

import java.util.List;

import com.dianping.puma.client.ChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

public interface EventFilterChain {
	public boolean doNext(ChangedEvent event, PumaContext context);

	public void setEventFilters(List<EventFilter> eventFilters);

}