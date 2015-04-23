package com.dianping.puma.core.monitor;

public class EventMonitor extends AbstractPumaMonitor {

	public EventMonitor() { super(); }

	public EventMonitor(String type, Long countThreshold) {
		super(type, countThreshold);
	}

	@Override
	public void record(String name, String status) {
		if (!isStopped()) {
			startCountingIfNeeded(name);
			incrCountingIfExists(name);

			if (checkCountingIfExists(name)) {
				resetCountingIfExists(name);
				monitor.logEvent(type, name, status, "");
			}
		}
	}

	@Override
	protected void doStart() {}

	@Override
	protected void doStop() {}

	@Override
	protected void doPause() {}
}
