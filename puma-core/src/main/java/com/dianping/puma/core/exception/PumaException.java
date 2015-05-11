package com.dianping.puma.core.exception;

public class PumaException extends Exception {

	public PumaException() {
		super();
	}

	public PumaException(String msg) {
		super(msg);
	}

	public PumaException(Throwable e) {
		super(e);
	}

	public PumaException(String msg, Throwable e) {
		super(msg, e);
	}
}
