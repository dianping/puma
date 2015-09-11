package com.dianping.puma.pumaserver.exception.binlog;

import com.dianping.puma.pumaserver.exception.PumaServerException;

public class BinlogChannelException extends PumaServerException {

	/**
	 * 
	 */
   private static final long serialVersionUID = 358277771824848545L;

	public BinlogChannelException(String msg) {
		super(msg);
	}

	public BinlogChannelException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
