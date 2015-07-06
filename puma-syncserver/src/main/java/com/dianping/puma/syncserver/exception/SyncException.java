package com.dianping.puma.syncserver.exception;

public class SyncException extends RuntimeException {

	public SyncException() {

	}

	public SyncException(String msg) {
		super(msg);
	}

	public SyncException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
