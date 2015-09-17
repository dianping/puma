/**
 * Project: puma-server
 *
 * File Created at 2012-7-7
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
package com.dianping.puma.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Comment of EventFilterChainFactory
 *
 * @author Leo Liang
 */
public final class EventFilterChainFactory {
	private EventFilterChainFactory() {

	}

	public static EventFilterChain createEventFilterChain(boolean needDdl, boolean needDml, boolean needTsInfo,
	      String[] dts) throws IllegalArgumentException {
		try {
			List<EventFilter> eventFilterList = new ArrayList<EventFilter>();
			// tsInfoFilter should be first
			TransactionInfoEventFilter tsInfoFilter = new TransactionInfoEventFilter();
			tsInfoFilter.init(needTsInfo);
			eventFilterList.add(tsInfoFilter);

			DbTbEventFilter dbtbFilter = new DbTbEventFilter();
			dbtbFilter.init(dts);
			eventFilterList.add(dbtbFilter);

			DmlDdlEventFilter dmlDdlFilter = new DmlDdlEventFilter();
			dmlDdlFilter.init(needDdl, needDml);
			eventFilterList.add(dmlDdlFilter);

			EventFilterChain chain = new DefaultEventFilterChain();
			chain.setEventFilters(eventFilterList);

			return chain;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
