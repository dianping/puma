package com.dianping.puma.core.exception;

public class HeartbeatException extends PumaException {
	/**
	 * 
	 */
   private static final long serialVersionUID = 6849086140041823181L;

	public HeartbeatException() {
		super();
	}

	public HeartbeatException(String msg) {
		super(msg);
	}

	public HeartbeatException(Throwable e) {
		super(e);
	}

	public HeartbeatException(String msg, Throwable e) {
		super(msg, e);
	}
}
