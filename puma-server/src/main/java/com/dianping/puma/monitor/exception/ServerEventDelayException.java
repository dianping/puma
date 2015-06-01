package com.dianping.puma.monitor.exception;

import com.dianping.puma.core.exception.MonitorException;

public class ServerEventDelayException extends MonitorException {
	public ServerEventDelayException() {
		super();
	}

	public ServerEventDelayException(String msg) {
		super(msg);
	}

	public ServerEventDelayException(Throwable e) {
		super(e);
	}

	public ServerEventDelayException(String msg, Throwable e) {
		super(msg, e);
	}
}
