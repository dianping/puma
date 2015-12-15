package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;

public interface EventFilter {

	boolean accept(ChangedEvent changedEvent, EventFilterChain eventfilterChain);

}