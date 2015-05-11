package com.dianping.puma.core.exception;

public class MonitorException extends PumaException {

	public MonitorException() {
		super();
	}

	public MonitorException(String msg) {
		super(msg);
	}

	public MonitorException(Throwable e) {
		super(e);
	}
	
	public MonitorException(String msg, Throwable e) {
		super(msg, e);
	}
}
