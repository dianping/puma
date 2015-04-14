package com.dianping.puma.monitor.exception;

public class MonitorThresholdException extends Exception {

	private static final long serialVersionUID = 4539077606952250454L;

	public MonitorThresholdException() {
		super();
	}

	public MonitorThresholdException(String message, Throwable t) {
		super(message, t);
	}
}
