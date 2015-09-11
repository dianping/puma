package com.dianping.puma.pumaserver.exception.binlog;

import com.dianping.puma.pumaserver.exception.PumaServerException;

public class BinlogAckException extends PumaServerException {

	/**
	 * 
	 */
   private static final long serialVersionUID = -4618190621912919550L;

	public BinlogAckException(String msg) {
		super(msg);
	}

	public BinlogAckException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
