package com.dianping.puma.filter;

import java.util.List;

import com.dianping.puma.core.event.Event;

public class DefaultEventFilterChain implements EventFilterChain {

	private int					pos	= 0;

	private List<EventFilter>	eventFilters;

	public boolean doNext(Event event) {

		if (eventFilters != null && pos < eventFilters.size()) {
			return eventFilters.get(pos++).accept(event, this);
		}

		return true;
	}

	@Override
	public void setEventFilters(List<EventFilter> eventFilters) {
		this.eventFilters = eventFilters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.filter.EventFilterChain#reset()
	 */
	@Override
	public void reset() {
		pos = 0;
	}

}