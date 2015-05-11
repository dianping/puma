package com.dianping.puma.server.exception;

import com.dianping.puma.core.exception.PumaException;

public class ServerEventParserException extends PumaException {
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
