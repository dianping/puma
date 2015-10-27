package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class TransactionInfoEventFilter implements EventFilter {

	private boolean needTsInfo = false;

	public void init(boolean needTsInfo) {
		this.needTsInfo = needTsInfo;
	}

	public boolean accept(ChangedEvent event, EventFilterChain eventfilterChain) {

		if (event instanceof RowChangedEvent) {
			RowChangedEvent rowEvent = (RowChangedEvent) event;
			if (rowEvent.isTransactionBegin() || rowEvent.isTransactionCommit()) {
				return needTsInfo && eventfilterChain.doNext(event);
			} else {
				return eventfilterChain.doNext(event);
			}
		} else {
			return eventfilterChain.doNext(event);
		}

	}

}