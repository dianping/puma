package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.event.ChangedEvent;

public class ConcurrentLoader implements Loader {

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public LoadFuture load(ChangedEvent binlogEvent) {
		return null;
	}
}
