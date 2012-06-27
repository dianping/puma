package com.dianping.puma.filter;

import com.dianping.puma.client.DataChangedEvent;

public class DefaultEventFilterChain implements EventFilterChain{
	
	private int pos=0;
	
	public void doNext(DataChangedEvent event) {
	}
	
}