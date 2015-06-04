package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.syncserver.job.LifeCycle;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;

public interface BatchExecPool extends LifeCycle {

	void put(BatchRow batchRow) throws InterruptedException;
}
