package com.dianping.puma.core.monitor;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;

public class EventMonitor {

	private String type;

	// Default logEvent interval is 1.
	private long interval;

	private long count = 0L;

	public EventMonitor() {
		this.interval = 1L;
	}

	public EventMonitor(long interval) {
		this.interval = interval;
	}

	public void log(String name) {
		if (doCount()) {
			Cat.logEvent(this.type, name);
		}
	}

	public void logSuccess(String name) {
		if (doCount()) {
			Cat.logEvent(this.type, name, Event.SUCCESS, "");
		}
	}

	public void logFailure(String name) {
		if (doCount()) {
			Cat.logEvent(this.type, name, "1", "");
		}
	}

	private synchronized boolean doCount() {
		if (++this.count == this.interval) {
			this.count = 0;
			return true;
		} else {
			return false;
		}
	}
}
