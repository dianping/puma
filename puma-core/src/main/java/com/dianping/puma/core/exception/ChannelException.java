package com.dianping.puma.core.exception;

public class ChannelException extends PumaException {
	/**
	 * 
	 */
   private static final long serialVersionUID = 3213949848013139538L;

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
