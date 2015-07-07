package com.dianping.puma.syncserver.task.exception;

import com.dianping.puma.syncserver.exception.SyncException;

public class TaskException extends SyncException {

	public TaskException() {

	}

	public TaskException(String msg) {
		super(msg);
	}

	public TaskException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
