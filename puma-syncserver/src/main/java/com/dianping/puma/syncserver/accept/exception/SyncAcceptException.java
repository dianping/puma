package com.dianping.puma.syncserver.accept.exception;

import com.dianping.puma.syncserver.exception.SyncException;

public class SyncAcceptException extends SyncException {

	public SyncAcceptException() {

	}

	public SyncAcceptException(String msg) {
		super(msg);
	}

	public SyncAcceptException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
