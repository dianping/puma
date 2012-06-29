package com.dianping.puma.filter;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

public abstract class AbstractEventFilter implements EventFilter {
	public boolean accept(DataChangedEvent event, EventFilterChain eventfilterChain, PumaContext context) {

		if (checkEvent(event)) {
			return eventfilterChain.doNext(event, context);
		} else {
			return false;
		}

	}

	protected abstract boolean checkEvent(DataChangedEvent event);

}