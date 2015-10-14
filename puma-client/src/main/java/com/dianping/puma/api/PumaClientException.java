package com.dianping.puma.api;

/**
 * PumaClientException
 */
public class PumaClientException extends RuntimeException {
	public PumaClientException() {
	}

	public PumaClientException(String message) {
		super(message);
	}

	public PumaClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
