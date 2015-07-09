package com.dianping.puma.pumaserver.exception.binlog;

import com.dianping.puma.pumaserver.exception.PumaServerException;

public class BinlogTargetException extends PumaServerException {

	/**
	 * 
	 */
   private static final long serialVersionUID = 5655029601262495879L;

	public BinlogTargetException(String msg) {
		super(msg);
	}

	public BinlogTargetException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
