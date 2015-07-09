package com.dianping.puma.pumaserver.exception.binlog;

import com.dianping.puma.pumaserver.exception.PumaServerException;

public class BinlogAuthException extends PumaServerException {

	/**
	 * 
	 */
   private static final long serialVersionUID = -6527741972972788734L;

	public BinlogAuthException(String msg) {
		super(msg);
	}

	public BinlogAuthException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
