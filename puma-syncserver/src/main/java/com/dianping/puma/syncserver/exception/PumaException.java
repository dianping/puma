package com.dianping.puma.syncserver.exception;

public class PumaException extends RuntimeException {

	public PumaException() {
	}

	public PumaException(String msg) {
		super(msg);
	}

	public PumaException(Throwable cause) {
		super(cause);
	}

	public PumaException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
