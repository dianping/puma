package com.dianping.puma.filter;

import com.dianping.puma.client.DataChangedEvent;


public interface EventFilterChain 
{
	public void doNext(DataChangedEvent event);
}