package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.syncserver.common.binlog.BinlogEvent;
import com.dianping.puma.syncserver.common.binlog.DdlEvent;

public class DdlCondition implements Condition {

	protected DdlEvent ddlEvent;

	@Override
	public synchronized void reset() {
		ddlEvent = null;
	}

	@Override
	public synchronized boolean isLocked(BinlogEvent binlogEvent) {
		return ddlEvent != null;
	}

	@Override
	public synchronized void lock(BinlogEvent binlogEvent) {
		if (binlogEvent instanceof DdlEvent) {
			if (ddlEvent != null) {
				throw new RuntimeException("ddl condition lock failure.");
			}

			ddlEvent = (DdlEvent) binlogEvent;
		}
	}

	@Override
	public synchronized void unlock(BinlogEvent binlogEvent) {
		if (binlogEvent instanceof DdlEvent) {
			if (ddlEvent == null || !ddlEvent.equals(binlogEvent)) {
				throw new RuntimeException("ddl condition unlock failure.");
			}

			ddlEvent = null;
		}
	}
}
