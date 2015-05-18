package com.dianping.puma.core.exception;

public class ChannelException extends PumaException {
	public ChannelException() {
		super();
	}

	public ChannelException(String msg) {
		super(msg);
	}

	public ChannelException(Throwable e) {
		super(e);
	}

	public ChannelException(String msg, Throwable e) {
		super(msg, e);
	}
}
