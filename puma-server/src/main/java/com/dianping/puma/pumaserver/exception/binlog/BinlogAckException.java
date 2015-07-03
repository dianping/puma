package com.dianping.puma.pumaserver.exception.binlog;

import com.dianping.puma.pumaserver.exception.PumaServerException;

public class BinlogAckException extends PumaServerException {

	public BinlogAckException(String msg) {
		super(msg);
	}

	public BinlogAckException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
