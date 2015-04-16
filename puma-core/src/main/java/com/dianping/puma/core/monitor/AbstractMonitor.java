package com.dianping.puma.core.monitor;

import java.util.concurrent.ConcurrentMap;

public abstract class AbstractMonitor implements Monitor {

	private String type;

	private ConcurrentMap<String, Long> counts;

	private Long countThreshold;

	private boolean stopped = true;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	protected void incrCountingIfExists(String name) {
		Long count = counts.get(name);
		if (count != null && count.equals(countThreshold)) {
			++count;
		}
	}

	protected void startCountingIfNeeded(String name) {
		counts.putIfAbsent(name, 0L);
	}

	protected void resetCountingIfExists(String name) {
		counts.put(name, 0L);
	}

	protected boolean checkCountingIfExists(String name) {
		Long count = counts.get(name);
		return count != null && count.equals(countThreshold);
	}

	protected boolean isStopped() {
		return stopped;
	}

	protected abstract void doStart();

	protected abstract void doStop();

	@Override
	public void start() {
		stopped = false;
		doStart();
	}

	@Override
	public void stop() {
		stopped = true;
		doStop();
	}
}
