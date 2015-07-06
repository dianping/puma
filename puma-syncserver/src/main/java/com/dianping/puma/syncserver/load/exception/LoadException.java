package com.dianping.puma.syncserver.load.exception;

import com.dianping.puma.syncserver.exception.SyncException;

public class LoadException extends SyncException {

	public LoadException() {

	}

	public LoadException(String msg) {
		super(msg);
	}

	public LoadException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
