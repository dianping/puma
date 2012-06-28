package com.dianping.puma.filter;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

public interface EventFilter {

	public boolean accept(DataChangedEvent event,
			EventFilterChain eventfilterChain, PumaContext context);

}