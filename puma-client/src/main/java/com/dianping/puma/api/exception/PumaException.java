package com.dianping.puma.api.exception;

public class PumaException extends RuntimeException {

	private String clientName;
	private String serverHost;

	public PumaException(String clientName, String msg) {

	}

	public PumaException(String msg, Throwable e) {
		super(msg, e);
	}

	public PumaException(String clientName, String serverHost, String msg) {
		super(msg);

		this.clientName = clientName;
		this.serverHost = serverHost;
	}

	public PumaException(String clientName, String msg, Throwable cause) {

	}

	public PumaException(String clientName, String serverHost, String msg, Throwable cause) {
		super(msg, cause);

		this.clientName = clientName;
		this.serverHost = serverHost;
	}
}
