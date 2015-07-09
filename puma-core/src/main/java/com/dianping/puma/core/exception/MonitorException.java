package com.dianping.puma.core.exception;

public class MonitorException extends PumaException {

	/**
	 * 
	 */
   private static final long serialVersionUID = 3754328888520849994L;

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
