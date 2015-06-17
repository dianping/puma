package com.dianping.puma.core.event;

public class ServerErrorEvent extends Event {

	private String msg;

	private Throwable cause;

	public ServerErrorEvent(String msg) {
		this.msg = msg;
	}

	public ServerErrorEvent(String msg, Throwable cause) {
		this.msg = msg;
		this.cause = cause;
	}

	public String getMsg() {
		return msg;
	}

	public Throwable getCause() {
		return cause;
	}
}
