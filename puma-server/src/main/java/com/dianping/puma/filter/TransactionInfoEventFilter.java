package com.dianping.puma.filter;

import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;

public class TransactionInfoEventFilter implements EventFilter {

	private boolean	needTsInfo	= false;

	public void init(boolean needTsInfo) {
		this.needTsInfo = needTsInfo;
	}

	public boolean accept(Event event, EventFilterChain eventfilterChain) {

		if (event instanceof RowChangedEvent) {
			RowChangedEvent rowEvent = (RowChangedEvent) event;
			if (rowEvent.isTransactionBegin() || rowEvent.isTransactionCommit()) {
				return needTsInfo;
			} else {
				return eventfilterChain.doNext(event);
			}
		} else {
			return eventfilterChain.doNext(event);
		}

	}

}