package com.dianping.puma.biz.monitor;

import com.dianping.puma.core.util.PumaThreadPool;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HeartbeatMonitor extends AbstractPumaMonitor {

	private int periodSeconds;

	private MonitorCore core;

	private ScheduledFuture scheduledFuture;

	public HeartbeatMonitor() {
	}

	@Override
	public void record(Object name, Object status) {
		core.put(name, status);
	}

	@Override
	public void remove(Object name) {
		core.remove(name);
	}

	@Override
	protected void doStart() {
		this.scheduledFuture = PumaThreadPool.schedule(new Runnable() {
			@Override
			public void run() {
				core.log();
			}
		}, 0, periodSeconds, TimeUnit.SECONDS);
	}

	@Override
	protected void doStop() {
		core.clear();
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

	public void setCore(MonitorCore core) {
		this.core = core;
	}

	public void setPeriodSeconds(int periodSeconds) {
		this.periodSeconds = periodSeconds;
	}
}
