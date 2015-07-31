package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.syncserver.common.binlog.BinlogEvent;

public class VolCondition implements Condition {

	protected final int volume;

	protected int count;

	public VolCondition(int volume) {
		this.volume = volume;
	}

	@Override
	public synchronized void reset() {
		count = 0;
	}

	@Override
	public synchronized boolean isLocked(BinlogEvent binlogEvent) {
		return count >= volume;
	}

	@Override
	public synchronized void lock(BinlogEvent binlogEvent) {
		if (count >= volume) {
			throw new RuntimeException("volume condition lock failure.");
		}
		++count;
	}

	@Override
	public synchronized void unlock(BinlogEvent binlogEvent) {
		if (count == 0) {
			throw new RuntimeException("volume condition unlock failure.");
		}
		--count;
	}
}
