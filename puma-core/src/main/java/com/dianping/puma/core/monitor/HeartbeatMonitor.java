package com.dianping.puma.core.monitor;

import com.dianping.cat.Cat;
import com.dianping.puma.core.util.PumaThreadPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HeartbeatMonitor extends AbstractMonitor {

	private int delaySeconds;

	private int periodSeconds;

	private ScheduledFuture scheduledFuture;

	private ConcurrentMap<String, String> statuses = new ConcurrentHashMap<String, String>();

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
	protected void doStart() {
		this.scheduledFuture = PumaThreadPool.schedule(new Runnable() {
			@Override
			public void run() {
				for (Map.Entry entry: statuses.entrySet()) {
					Cat.logEvent(type, (String) entry.getKey(), (String) entry.getValue(), "");
				}
			}
		}, delaySeconds, periodSeconds, TimeUnit.SECONDS);
	}

	@Override
	protected void doStop() {
		if (this.scheduledFuture != null && !this.scheduledFuture.isCancelled()) {
			this.scheduledFuture.cancel(true);
		}
	}
}
