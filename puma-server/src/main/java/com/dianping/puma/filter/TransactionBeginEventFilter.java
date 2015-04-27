package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class TransactionBeginEventFilter extends AbstractEventFilter {

	protected boolean checkEvent(Event event) {
		return !(event instanceof RowChangedEvent) || !((RowChangedEvent) event).isTransactionBegin();
	}
}
