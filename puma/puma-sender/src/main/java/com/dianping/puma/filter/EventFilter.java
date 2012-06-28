package com.dianping.puma.filter;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

public interface EventFilter{
	
	public void doFilter(DataChangedEvent event, EventFilterChain eventfilterChain, PumaContext	context);
	
	public void initEventFilters();
	
}