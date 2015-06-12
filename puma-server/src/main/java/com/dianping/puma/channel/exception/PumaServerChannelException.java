package com.dianping.puma.channel.exception;

public class PumaServerChannelException extends RuntimeException {

	public PumaServerChannelException(String msg) {
		super(msg);
	}

	public PumaServerChannelException(String msg, Throwable e) {
		super(msg, e);
	}
}
