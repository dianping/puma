package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;

public interface Loader extends LifeCycle<Exception> {

	LoadFuture load(ChangedEvent event);
}
