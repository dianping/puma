package com.dianping.puma.biz.monitor;

public class EventMonitor extends AbstractPumaMonitor {

	public EventMonitor() {
		super();
	}

	public EventMonitor(String type, Long countThreshold) {
		super(type, countThreshold);
	}

	@Override
	public void record(Object name, Object status) {
		if (!isStopped()) {
			startCountingIfNeeded((String) name);
			incrCountingIfExists((String) name);

			if (checkCountingIfExists((String) name)) {
				resetCountingIfExists((String) name);
				monitor.logEvent(type, (String) name, (String) status, "");
			}
		}
	}

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	protected void doPause() {
	}

	@Override
	public void remove(Object name) {

	}
}
