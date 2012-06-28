package com.dianping.puma.filter;

import java.util.List;
import org.apache.log4j.Logger;

public class EventFilterChainConfig {
	private static final Logger log = Logger
			.getLogger(EventFilterChainConfig.class);
	private static EventFilterChainConfig instance = new EventFilterChainConfig();

	
	private static final String CONFIG = "EventFilter.xml";

	private EventFilterChainConfig() {
		initEventFilters();
	}

	private void initEventFilters() {
		// TODO Auto-generated method stub
	}

	public List<EventFilter> getEventFilters() {
		return eventFilters;
	}

	public static EventFilterChainConfig getInstance() {
		return instance;
	}
}