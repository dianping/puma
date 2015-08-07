package com.dianping.puma.syncserver.executor.transform;

import com.dianping.puma.syncserver.common.LifeCycle;
import com.dianping.puma.syncserver.common.binlog.BinlogEvent;

public interface Transformer extends LifeCycle {

	public void transform(BinlogEvent binlogEvent);
}
