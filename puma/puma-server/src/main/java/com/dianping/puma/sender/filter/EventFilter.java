package com.dianping.puma.sender.filter;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;

public interface EventFilter {

	public boolean accept(ChangedEvent event, EventFilterChain eventfilterChain, PumaContext context);

}