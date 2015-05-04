package com.dianping.puma.syncserver.job.load.exception;

import org.apache.commons.lang3.exception.ContextedRuntimeException;

public class LoadException extends ContextedRuntimeException {

	private String errorCode;

	private String errorDesc;

	public LoadException(String errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public LoadException(String errorCode, String errorDesc) {
		super(errorCode + ":" + errorDesc);
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
	}

	public LoadException(String errorCode, String errorDesc, Throwable cause) {
		super(errorCode + ":" + errorDesc, cause);
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorDesc() {
		return errorDesc;
	}
}
