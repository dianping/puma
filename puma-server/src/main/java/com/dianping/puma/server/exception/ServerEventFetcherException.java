package com.dianping.puma.server.exception;

import com.dianping.puma.core.exception.PumaException;

public class ServerEventFetcherException extends PumaException {

	private static final long serialVersionUID = 3719251414447687703L;

	public ServerEventFetcherException() {
		super();
	}

	public ServerEventFetcherException(String msg) {
		super(msg);
	}

	public ServerEventFetcherException(Throwable e) {
		super(e);
	}

	public ServerEventFetcherException(String msg, Throwable e) {
		super(msg, e);
	}
}
