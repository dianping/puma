package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;

public abstract class AbstractEventFilter implements EventFilter {
	public boolean accept(ChangedEvent event, EventFilterChain eventfilterChain) {

		if (checkEvent(event)) {
			return eventfilterChain.doNext(event);
		} else {
			return false;
		}

	}

	protected abstract boolean checkEvent(ChangedEvent event);

}