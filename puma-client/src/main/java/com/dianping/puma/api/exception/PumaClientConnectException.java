package com.dianping.puma.api.exception;

import com.dianping.puma.core.exception.HeartbeatException;

public class PumaClientConnectException extends HeartbeatException {
	public PumaClientConnectException() {
		super();
	}

	public PumaClientConnectException(String msg) {
		super(msg);
	}

	public PumaClientConnectException(Throwable e) {
		super(e);
	}

	public PumaClientConnectException(String msg, Throwable e) {
		super(msg, e);
	}
}
