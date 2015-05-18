package com.dianping.puma.syncserver.job.executor.exception;

import org.apache.commons.lang3.exception.ContextedRuntimeException;

public class GException extends ContextedRuntimeException {

	private int errorCode;

	private String errorDesc;

	public GException(int errorCode) {
		super(String.valueOf(errorCode));
		this.errorCode = errorCode;
	}

	public GException(int errorCode, String errorDesc) {
		super(String.valueOf(errorCode) + ":" + errorDesc);
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
	}

	public GException(int errorCode, String errorDesc, Throwable cause) {
		super(String.valueOf(errorCode) + ":" + errorDesc, cause);
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorDesc() {
		return errorDesc;
	}
}
