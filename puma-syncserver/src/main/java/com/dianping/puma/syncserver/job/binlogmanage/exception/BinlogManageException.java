package com.dianping.puma.syncserver.job.binlogmanage.exception;

import org.apache.commons.lang3.exception.ContextedRuntimeException;

public class BinlogManageException extends ContextedRuntimeException {

	/**
	 * error code:
	 * 0: Loader already stopped error.
	 * 1: Thread interrupted error.
	 * rest: SQL error.
	 */
	private int errorCode;

	private String errorDesc;

	public BinlogManageException(int errorCode) {
		super(String.valueOf(errorCode));
		this.errorCode = errorCode;
	}

	public BinlogManageException(int errorCode, String errorDesc) {
		super(String.valueOf(errorCode) + ":" + errorDesc);
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
	}

	public BinlogManageException(int errorCode, String errorDesc, Throwable cause) {
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

	public static BinlogManageException translate(Exception e) {
		return new BinlogManageException(-1, e.getMessage(), e.getCause());
	}
}
