package com.dianping.puma.syncserver.exception;

public class PumaBinlogException extends RuntimeException {

	public PumaBinlogException() {

	}

	public PumaBinlogException(String msg) {
		super(msg);
	}

	public PumaBinlogException(Throwable cause) {
		super(cause);
	}

	public PumaBinlogException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
