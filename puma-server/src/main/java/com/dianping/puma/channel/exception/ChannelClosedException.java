package com.dianping.puma.channel.exception;

import com.dianping.puma.core.exception.ChannelException;

public class ChannelClosedException extends ChannelException {
	public ChannelClosedException() {
		super();
	}

	public ChannelClosedException(String msg) {
		super(msg);
	}

	public ChannelClosedException(Throwable e) {
		super(e);
	}

	public ChannelClosedException(String msg, Throwable e) {
		super(msg, e);
	}
}
