package com.dianping.puma.syncserver.job.executor.exception;

import java.sql.SQLException;

public class DdlRenameException extends SQLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5602610542252983435L;

	public DdlRenameException() {
		super();
	}

	public DdlRenameException(String message) {
		super(message);
	}
}
