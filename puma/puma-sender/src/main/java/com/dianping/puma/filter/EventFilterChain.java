package com.dianping.puma.filter;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;


public interface EventFilterChain 
{
	public void doNext(DataChangedEvent event, PumaContext context);
}