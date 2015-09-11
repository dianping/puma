package com.dianping.puma.pumaserver.exception;

public class PumaServerException extends RuntimeException {

	/**
	 * 
	 */
   private static final long serialVersionUID = -1323742087664742400L;

	public PumaServerException(String msg) {
		super(msg);
	}

	public PumaServerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
