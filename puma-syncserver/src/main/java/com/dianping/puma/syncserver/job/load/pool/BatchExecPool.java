package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.syncserver.job.Component;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;

public interface BatchExecPool extends Component {

	void asyncThrow() throws LoadException;

	void put(BatchRow batchRow) throws LoadException;

}
