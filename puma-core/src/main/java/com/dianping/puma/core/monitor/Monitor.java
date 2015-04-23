package com.dianping.puma.core.monitor;

import com.dianping.cat.Cat;

public class Monitor {

	public void logEvent(String type, String name) {
		Cat.logEvent(type, name);
	}

	public void logEvent(String type, String name, String status, String details) {
		Cat.logEvent(type, name, status, details);
	}
}
