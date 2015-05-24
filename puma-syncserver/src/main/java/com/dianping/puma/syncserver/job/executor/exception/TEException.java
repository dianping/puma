package com.dianping.puma.syncserver.job.executor.exception;

import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.transform.exception.TransformException;
import org.apache.commons.lang3.exception.ContextedRuntimeException;

public class TEException extends ContextedRuntimeException {

	private int errorCode;

	private String errorDesc;

	public TEException(int errorCode) {
		super(String.valueOf(errorCode));
		this.errorCode = errorCode;
	}

	public TEException(int errorCode, String errorDesc) {
		super(String.valueOf(errorCode) + ":" + errorDesc);
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
	}

	public TEException(int errorCode, String errorDesc, Throwable cause) {
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

	public static TEException translate(Exception e) {
		if (e instanceof TransformException) {
			TransformException te = (TransformException) e;
			return new TEException(te.getErrorCode(), te.getErrorDesc(), te.getCause());
		} else if (e instanceof LoadException) {
			LoadException le = (LoadException) e;
			return new TEException(le.getErrorCode(), le.getErrorDesc(), le.getCause());
		} else {
			return new TEException(-1, e.getMessage(), e.getCause());
		}
	}
}
