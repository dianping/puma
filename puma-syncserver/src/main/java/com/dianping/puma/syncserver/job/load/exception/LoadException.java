package com.dianping.puma.syncserver.job.load.exception;

import org.apache.commons.lang3.exception.ContextedRuntimeException;

import java.sql.SQLException;

public class LoadException extends ContextedRuntimeException {

	/**
	 * error code:
	 * 0: Loader already stopped error.
	 * 1: Thread interrupted error.
	 * rest: SQL error.
	 */
	private int errorCode;

	private String errorDesc;

	public LoadException(int errorCode) {
		super(String.valueOf(errorCode));
		this.errorCode = errorCode;
	}

	public LoadException(int errorCode, String errorDesc) {
		super(String.valueOf(errorCode) + ":" + errorDesc);
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
	}

	public LoadException(int errorCode, String errorDesc, Throwable cause) {
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

	public static LoadException translate(Exception e) {
		if (e instanceof SQLException) {
			SQLException se = (SQLException) e;
			return new LoadException(se.getErrorCode(), se.getMessage(), se.getCause());
		} else {
			return new LoadException(-1, e.getMessage(), e.getCause());
		}
	}
}
