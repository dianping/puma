package com.dianping.puma.syncserver.exception;

public class PumaBaseTaskException extends RuntimeException {

	public PumaBaseTaskException() {

	}

	public PumaBaseTaskException(String msg) {
		super(msg);
	}

	public PumaBaseTaskException(Throwable cause) {
		super(cause);
	}

	public PumaBaseTaskException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
