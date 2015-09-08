package com.dianping.puma.comparison;

import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class RowContext {

	private volatile long lastRetryTime;

	private volatile Map<String, Object> source;

	private volatile Map<String, Object> target;

	private volatile int tryTimes;

	public Map<String, Object> getSource() {
		return source;
	}

	public void setSource(Map<String, Object> source) {
		this.source = source;
	}

	public Map<String, Object> getTarget() {
		return target;
	}

	public void setTarget(Map<String, Object> target) {
		this.target = target;
	}

	public long getLastRetryTime() {
		return lastRetryTime;
	}

	public void setLastRetryTime(long lastRetryTime) {
		this.lastRetryTime = lastRetryTime;
	}

	public int getTryTimes() {
		return tryTimes;
	}

	public void increaseTryTimes() {
		this.tryTimes++;
	}
}
