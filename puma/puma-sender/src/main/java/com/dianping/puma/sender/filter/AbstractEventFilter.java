package com.dianping.puma.sender.filter;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;

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