package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.core.event.ChangedEvent;

public interface Loader {

	void load(ChangedEvent binlogEvent, LoadCallback callback);
}
