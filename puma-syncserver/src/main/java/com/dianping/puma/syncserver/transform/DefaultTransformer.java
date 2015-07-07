package com.dianping.puma.syncserver.transform;

import com.dianping.puma.core.event.ChangedEvent;

public class DefaultTransformer implements Transformer {

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public ChangedEvent transform(ChangedEvent binlogEvent) {
		return null;
	}
}
