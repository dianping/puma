package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.event.ChangedEvent;

public interface Loader {

	LoadFuture load(ChangedEvent event);
}
