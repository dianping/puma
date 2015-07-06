package com.dianping.puma.syncserver.transform.exception;

import com.dianping.puma.syncserver.exception.SyncException;

public class TransformException extends SyncException {

	public TransformException() {

	}

	public TransformException(String msg) {
		super(msg);
	}

	public TransformException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
