package com.dianping.puma.sender.filter;

import com.dianping.puma.client.ChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

public abstract class AbstractEventFilter implements EventFilter {
	public boolean accept(ChangedEvent event, EventFilterChain eventfilterChain, PumaContext context) {

		if (checkEvent(event)) {
			return eventfilterChain.doNext(event, context);
		} else {
			return false;
		}

	}

	protected abstract boolean checkEvent(ChangedEvent event);

}