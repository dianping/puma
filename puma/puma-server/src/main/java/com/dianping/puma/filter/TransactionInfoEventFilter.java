package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class TransactionInfoEventFilter implements EventFilter {

	private boolean	needTsInfo	= false;

	public void init(boolean needTsInfo) {
		this.needTsInfo = needTsInfo;
	}

	public boolean accept(ChangedEvent event, EventFilterChain eventfilterChain) {

		if (checkEvent(event)) {
			return true;
		} else {
			return false;
		}

	}

	protected boolean checkEvent(ChangedEvent event) {
		if (event instanceof RowChangedEvent) {
			RowChangedEvent rowEvent = (RowChangedEvent) event;
			if (!needTsInfo && (rowEvent.isTransactionBegin() || rowEvent.isTransactionCommit())) {
				return false;
			}
		}

		return true;
	}
}