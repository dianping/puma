package com.dianping.puma.monitor.exception;

import com.dianping.puma.core.exception.MonitorException;

public class ServerEventDelayMonitorException extends MonitorException {
	public ServerEventDelayMonitorException() {
		super();
	}

	public ServerEventDelayMonitorException(String msg) {
		super(msg);
	}

	public ServerEventDelayMonitorException(Throwable e) {
		super(e);
	}

	public ServerEventDelayMonitorException(String msg, Throwable e) {
		super(msg, e);
	}
}
