package com.dianping.puma.core.monitor;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;

import java.util.concurrent.atomic.AtomicLong;

/*
public class EventMonitor {

	private String type;

	private long interval = 1L;

	private AtomicLong count;

	public EventMonitor() {}

	public EventMonitor(long interval) {
		this.interval = interval;
	}

	public void log(String name) {
		if (count.incrementAndGet() % interval == 0) {
			count.set();
			Cat.logEvent(this.type, name);
		}
	}

	public void logSuccess(String name, String detail) {
		Cat.logEvent(this.type, name, Event.SUCCESS, detail);
	}

	public void logFailure(String name, String detail) {
		Cat.logEvent(this.type, name, "1", detail);
	}
}*/
