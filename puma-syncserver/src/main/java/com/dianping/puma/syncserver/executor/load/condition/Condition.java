package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.core.event.ChangedEvent;

public interface Condition {

	@ThreadSafe
	public void reset();

	@ThreadSafe
	public boolean isLocked(ChangedEvent binlogEvent);

	@ThreadSafe
	public void lock(ChangedEvent binlogEvent);

	@ThreadSafe
	public void unlock(ChangedEvent binlogEvent);
}
