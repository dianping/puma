package com.dianping.puma.pumaserver.exception.binlog;

import com.dianping.puma.pumaserver.exception.PumaServerException;

public class BinlogTargetException extends PumaServerException {

	public BinlogTargetException(String msg) {
		super(msg);
	}

	public BinlogTargetException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
