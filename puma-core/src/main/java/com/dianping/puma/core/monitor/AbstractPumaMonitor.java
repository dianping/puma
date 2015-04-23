package com.dianping.puma.core.monitor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractPumaMonitor implements PumaMonitor {

	protected String type;

	protected Monitor monitor;

	private ConcurrentMap<String, Long> counts = new ConcurrentHashMap<String, Long>();

	private Long countThreshold;

	private Long countThresholdCache;

	private boolean stopped = true;

	public AbstractPumaMonitor() {}

	public AbstractPumaMonitor(String type) {
		this.type = type;
		this.countThreshold = 1L;
	}

	public AbstractPumaMonitor(String type, Long countThreshold) {
		this.type = type;
		this.countThreshold = countThreshold;
	}

	protected void incrCountingIfExists(String name) {
		Long count = counts.get(name);
		if (count != null) {
			counts.put(name, count + 1);
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
		return count != null && count.equals(countThresholdCache);
	}

	public boolean isStopped() {
		return stopped;
	}

	protected abstract void doStart();

	protected abstract void doStop();

	protected abstract void doPause();

	@Override
	public void start() {
		countThresholdCache = countThreshold;
		stopped = false;
		doStart();
	}

	@Override
	public void stop() {
		stopped = true;
		counts.clear();
		doStop();
	}

	@Override
	public void pause() {
		stopped = true;
		doPause();
	}

	public void setType(String type) {
		this.type = type;
	}

	// Stop and start the monitor to valid new setting count threshold.
	public void setCountThreshold(Long countThreshold) {
		this.countThreshold = countThreshold;
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}
}
