package com.dianping.puma.filter;

import java.util.List;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

public interface EventFilterChain {
	public boolean doNext(DataChangedEvent event, PumaContext context);

	public void setEventFilters(List<EventFilter> eventFilters);

}