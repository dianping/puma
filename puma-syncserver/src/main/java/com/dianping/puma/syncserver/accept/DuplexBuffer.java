package com.dianping.puma.syncserver.accept;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.load.LoadFuture;

public interface DuplexBuffer extends LifeCycle<Exception> {

	void putQuery(ChangedEvent binlogEvent);

	ChangedEvent pollQuery();

	void putResult(LoadFuture loadFuture);

	LoadFuture pollResult();
}
