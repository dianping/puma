package com.dianping.puma.filter;

import java.util.List;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

public class DefaultEventFilterChain implements EventFilterChain {

	private int					pos	= 0;

	private List<EventFilter>	eventFilters;

	public boolean doNext(DataChangedEvent event, PumaContext context) {

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