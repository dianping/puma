package com.dianping.puma.api;

import com.dianping.puma.core.exception.HeartbeatException;

public class HeartbeatLosedException extends HeartbeatException {
	public HeartbeatLosedException() {
		super();
	}

	public HeartbeatLosedException(String msg) {
		super(msg);
	}

	public HeartbeatLosedException(Throwable e) {
		super(e);
	}

	public HeartbeatLosedException(String msg, Throwable e) {
		super(msg, e);
	}
}
