/**
 * Project: hippo-collector
 * 
 * File Created at 2011-11-1
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.sender.filter;

import java.util.List;

/**
 * TODO Comment of EventFilterChainConfig
 * 
 * @author Leo Liang
 * 
 */
public class EventFilterChainConfig {

	private List<EventFilter>	eventFilters;

	public void setEventFilters(List<EventFilter> eventFilters) {
		this.eventFilters = eventFilters;
	}

	public List<EventFilter> getEventFilters() {
		return eventFilters;
	}

}
