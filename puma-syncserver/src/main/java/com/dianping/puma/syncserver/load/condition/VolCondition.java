package com.dianping.puma.syncserver.load.condition;

import com.dianping.puma.core.event.ChangedEvent;

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
	public synchronized boolean isLocked(ChangedEvent binlogEvent) {
		return count >= volume;
	}

	@Override
	public synchronized void lock(ChangedEvent binlogEvent) {
		if (count >= volume) {
			throw new RuntimeException("volume condition lock failure.");
		}
		++count;
	}

	@Override
	public synchronized void unlock(ChangedEvent binlogEvent) {
		if (count == 0) {
			throw new RuntimeException("volume condition unlock failure.");
		}
		--count;
	}
}
