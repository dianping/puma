package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.syncserver.common.binlog.BinlogEvent;

public interface Condition {

	@ThreadSafe
	public void reset();

	@ThreadSafe
	public boolean isLocked(BinlogEvent binlogEvent);

	@ThreadSafe
	public void lock(BinlogEvent binlogEvent);

	@ThreadSafe
	public void unlock(BinlogEvent binlogEvent);
}
