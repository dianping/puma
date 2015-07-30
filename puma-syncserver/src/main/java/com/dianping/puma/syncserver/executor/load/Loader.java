package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.common.LifeCycle;

public interface Loader extends LifeCycle {

	public LoadFuture load(ChangedEvent binlogEvent);
}
