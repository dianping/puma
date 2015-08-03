package com.dianping.puma.server.exception;

import com.dianping.puma.core.exception.PumaException;

public class ServerEventRuntimeException extends PumaException {

	private static final long serialVersionUID = -5034748008327192271L;

	public ServerEventRuntimeException() {
		super();
	}

	public ServerEventRuntimeException(String msg) {
		super(msg);
	}

	public ServerEventRuntimeException(Throwable e) {
		super(e);
	}

	public ServerEventRuntimeException(String msg, Throwable e) {
		super(msg, e);
	}
}
