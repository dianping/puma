package com.dianping.puma.sender.filter;

import java.util.List;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;

public class DefaultEventFilterChain implements EventFilterChain {

	private int					pos	= 0;

	private List<EventFilter>	eventFilters;

	public boolean doNext(ChangedEvent event, PumaContext context) {

		if (eventFilters != null && pos < eventFilters.size()) {
			return eventFilters.get(pos++).accept(event, this, context);
		}

		return true;
	}

	@Override
	public void setEventFilters(List<EventFilter> eventFilters) {
		this.eventFilters = eventFilters;
	}

}