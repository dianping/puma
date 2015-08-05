package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.syncserver.common.LifeCycle;
import com.dianping.puma.syncserver.common.binlog.BinlogEvent;

public interface Loader extends LifeCycle {

	public LoadFuture load(BinlogEvent binlogEvent);
}
