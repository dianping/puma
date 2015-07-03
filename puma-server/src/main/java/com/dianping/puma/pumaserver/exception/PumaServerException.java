package com.dianping.puma.pumaserver.exception;

public class PumaServerException extends RuntimeException {

	public PumaServerException(String msg) {
		super(msg);
	}

	public PumaServerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
