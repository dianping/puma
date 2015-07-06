package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.load.exception.LoadException;

public interface Loader extends LifeCycle<LoadException> {

	LoadFuture load(ChangedEvent event) throws LoadException;
}
