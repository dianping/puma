package com.dianping.puma.sender.filter;

import com.dianping.puma.client.ChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

public interface EventFilter {

	public boolean accept(ChangedEvent event, EventFilterChain eventfilterChain, PumaContext context);

}