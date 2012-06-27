package com.dianping.puma.filter;

import com.dianping.puma.client.DataChangedEvent;

public interface EventFilter{
	
	public void doFilter(DataChangedEvent event, EventFilterChain eventfilterChian);
	
}