package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class DmlDdlEventFilter extends AbstractEventFilter {

	private int operationType = 0;

	public void init(boolean needDdl, boolean needDml) {

		if (needDdl) {
			operationType |= 1;
		}
		if (needDml) {
			operationType |= 2;
		}
	}

	protected boolean checkEvent(ChangedEvent event) {
		if ((event instanceof RowChangedEvent) && (operationType & 2) != 0) {
			return true;
		} else if ((event instanceof DdlEvent) && (operationType & 1) != 0) {
			return true;
		}
		return false;
	}
}