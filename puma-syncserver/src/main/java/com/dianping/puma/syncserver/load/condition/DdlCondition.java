package com.dianping.puma.syncserver.load.condition;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;

public class DdlCondition implements Condition {

	protected DdlEvent ddlEvent;

	@Override
	public synchronized void reset() {
		ddlEvent = null;
	}

	@Override
	public synchronized boolean isLocked(ChangedEvent binlogEvent) {
		return ddlEvent != null;
	}

	@Override
	public synchronized void lock(ChangedEvent binlogEvent) {
		if (binlogEvent instanceof DdlEvent) {
			if (ddlEvent != null) {
				throw new RuntimeException("ddl condition lock failure.");
			}

			ddlEvent = (DdlEvent) binlogEvent;
		}
	}

	@Override
	public synchronized void unlock(ChangedEvent binlogEvent) {
		if (binlogEvent instanceof DdlEvent) {
			if (ddlEvent == null || !ddlEvent.equals(binlogEvent)) {
				throw new RuntimeException("ddl condition unlock failure.");
			}

			ddlEvent = null;
		}
	}
}
