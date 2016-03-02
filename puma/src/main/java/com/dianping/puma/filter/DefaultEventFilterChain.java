package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;

import java.util.List;

public class DefaultEventFilterChain implements EventFilterChain {

	private int pos = 0;

	private List<EventFilter> eventFilters;

	public boolean doNext(ChangedEvent event) {

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