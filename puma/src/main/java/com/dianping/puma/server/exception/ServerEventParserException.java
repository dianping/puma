package com.dianping.puma.server.exception;

import com.dianping.puma.core.exception.PumaException;

public class ServerEventParserException extends PumaException {

	private static final long serialVersionUID = 7798043572476717953L;

	public ServerEventParserException() {
		super();
	}

	public ServerEventParserException(String msg) {
		super(msg);
	}

	public ServerEventParserException(Throwable e) {
		super(e);
	}

	public ServerEventParserException(String msg, Throwable e) {
		super(msg, e);
	}
}
