package com.dianping.puma.monitor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTaskMonitor {
	
	protected ScheduledExecutorService executor;

	protected long initialDelay;
	protected long period;
	protected TimeUnit unit;
	
	public AbstractTaskMonitor(long initialDelay,long period,TimeUnit unit){
		this.initialDelay=initialDelay;
		this.period=period;
		this.unit=unit;
	}
	
	public void setExecutor(ScheduledExecutorService executor) {
		this.executor = executor;
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	public void setInitialDelay(long initialDelay) {
		this.initialDelay = initialDelay;
	}

	public long getInitialDelay() {
		return initialDelay;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public long getPeriod() {
		return period;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}

	public TimeUnit getUnit() {
		return unit;
	}
	
	public void execute(){
		doExecute();
	}
	
	public abstract void doExecute();

}
