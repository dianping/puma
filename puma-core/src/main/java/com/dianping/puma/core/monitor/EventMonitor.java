package com.dianping.puma.core.monitor;

import com.dianping.cat.Cat;

public class EventMonitor extends AbstractMonitor {

	@Override
	public void record(String name, String status) {
		if (!isStopped()) {
			startCountingIfNeeded(name);
			incrCountingIfExists(name);

			if (checkCountingIfExists(name)) {
				Cat.logEvent(getType(), name, status, "");
			}
		}
	}

	@Override
	protected void doStart() {}

	@Override
	protected void doStop() {}
}
