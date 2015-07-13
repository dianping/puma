package com.dianping.puma.core.exception;

public class PumaException extends RuntimeException {

	/**
	 * 
	 */
   private static final long serialVersionUID = 7423790635936212910L;

	public PumaException() {
		super();
	}

	public PumaException(String msg) {
		super(msg);
	}

	public PumaException(Throwable e) {
		super(e);
	}

	public PumaException(String msg, Throwable e) {
		super(msg, e);
	}
}
