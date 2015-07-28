package com.dianping.puma.syncserver.load.condition;

import com.dianping.puma.core.event.ChangedEvent;

public interface Condition {

	public void reset();

	public boolean isLocked(ChangedEvent binlogEvent);

	public void lock(ChangedEvent binlogEvent);

	public void unlock(ChangedEvent binlogEvent);
}
