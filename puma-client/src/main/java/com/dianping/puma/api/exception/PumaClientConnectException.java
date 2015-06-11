package com.dianping.puma.api.exception;

public class PumaClientConnectException extends RuntimeException {

	public PumaClientConnectException(String msg) {
		super(msg);
	}

	public PumaClientConnectException(String msg, Throwable e) {
		super(msg, e);
	}
}
