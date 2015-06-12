package com.dianping.puma.api.exception;

public class PumaClientConfigException extends RuntimeException {

	public PumaClientConfigException(String msg) {
		super(msg);
	}

	public PumaClientConfigException(String msg, Throwable e) {
		super(msg, e);
	}
}
