package com.dianping.puma.syncserver.exception;

public class PumaTimeoutException extends Exception {

	public PumaTimeoutException() {

	}

	public PumaTimeoutException(String msg) {
		super(msg);
	}

	public PumaTimeoutException(Throwable cause) {
		super(cause);
	}

	public PumaTimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
