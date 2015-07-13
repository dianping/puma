package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.event.ChangedEvent;

public class ConcurrentLoader extends AbstractLoader {

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public LoadFuture load(ChangedEvent binlogEvent) {
		return null;
	}
}
