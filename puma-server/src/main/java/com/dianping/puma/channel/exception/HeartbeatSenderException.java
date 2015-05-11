package com.dianping.puma.channel.exception;

import com.dianping.puma.core.exception.HeartbeatException;

public class HeartbeatSenderException extends HeartbeatException {
	public HeartbeatSenderException() {
		super();
	}

	public HeartbeatSenderException(String msg) {
		super(msg);
	}

	public HeartbeatSenderException(Throwable e) {
		super(e);
	}

	public HeartbeatSenderException(String msg, Throwable e) {
		super(msg, e);
	}
}
