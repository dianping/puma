package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.LifeCycle;
import com.dianping.puma.syncserver.job.load.exception.LoadException;

public interface Loader extends LifeCycle {

	void asyncThrow() throws LoadException;

	void load(ChangedEvent event) throws LoadException;
}
