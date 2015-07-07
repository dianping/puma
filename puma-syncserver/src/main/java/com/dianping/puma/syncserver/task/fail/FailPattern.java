package com.dianping.puma.syncserver.task.fail;

import java.util.concurrent.TimeUnit;

public class FailPattern {

	private volatile boolean retry;

	private volatile int times;

	private volatile int period;

	private volatile TimeUnit timeUnit;

	public FailPattern(boolean retry, int times, int period, TimeUnit timeUnit) {
		this.retry = retry;
		this.times = times;
		this.period = period;
		this.timeUnit = timeUnit;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
}
