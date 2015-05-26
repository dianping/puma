package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.Component;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;

public interface BatchRowPool extends Component {

	void put(ChangedEvent event) throws LoadException;

	BatchRow take() throws LoadException;
}
