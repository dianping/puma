package com.dianping.puma.filter;

import java.util.List;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;


public class DefaultEventFilterChain implements EventFilterChain{
	
	private int pos=0;
	
	private boolean accept=false;

	private List<EventFilter> eventFilters;
	
	public void doNext(DataChangedEvent event, PumaContext	context) {
		
		if (EventFilterChainConfig.getInstance().getEventFilters() != null
				&& pos < EventFilterChainConfig.getInstance().getEventFilters().size()) {
			EventFilterChainConfig.getInstance().getEventFilters().get(pos++).doFilter(event,this, context);
		}
		
		if( pos == EventFilterChainConfig.getInstance().getEventFilters().size())
		{
			accept=true;
		}
		
		
	}
	
}