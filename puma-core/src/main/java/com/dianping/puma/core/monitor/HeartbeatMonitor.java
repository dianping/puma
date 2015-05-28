package com.dianping.puma.core.monitor;

import com.dianping.puma.core.util.PumaThreadPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HeartbeatMonitor extends AbstractPumaMonitor {

	private int delaySeconds;

	private int periodSeconds;

	private ScheduledFuture scheduledFuture;

	private ConcurrentMap<String, String> statuses = new ConcurrentHashMap<String, String>();

	public HeartbeatMonitor() {
		super();
	}

	public HeartbeatMonitor(String type, int delaySeconds, int periodSeconds) {
		super(type);
		this.delaySeconds = delaySeconds;
		this.periodSeconds = periodSeconds;
	}

	@Override
	public void record(String name, String status) {
		if (!isStopped()) {
			statuses.put(name, status);
		}
	}
	
	@Override
	public void remove(String name) {
		if (!isStopped()) {
			statuses.remove(name);
		}
	}

	@Override
	protected void doStart() {
		this.scheduledFuture = PumaThreadPool.schedule(new Runnable() {
			@Override
			public void run() {
				for (Map.Entry entry: statuses.entrySet()) {
					monitor.logEvent(type, (String) entry.getKey(), (String) entry.getValue(), "");
					statuses.put((String) entry.getKey(), "0");
				}
			}
		}, delaySeconds, periodSeconds, TimeUnit.SECONDS);
	}

	@Override
	protected void doStop() {
		statuses.clear();
		if (this.scheduledFuture != null && !this.scheduledFuture.isCancelled()) {
			this.scheduledFuture.cancel(true);
		}
	}

	@Override
	protected void doPause() {
		if (this.scheduledFuture != null && !this.scheduledFuture.isCancelled()) {
			this.scheduledFuture.cancel(true);
		}
	}

	public void setDelaySeconds(int delaySeconds) {
		this.delaySeconds = delaySeconds;
	}

	public void setPeriodSeconds(int periodSeconds) {
		this.periodSeconds = periodSeconds;
	}
}
