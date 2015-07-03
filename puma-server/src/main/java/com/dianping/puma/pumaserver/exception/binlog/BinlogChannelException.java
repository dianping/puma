package com.dianping.puma.pumaserver.exception.binlog;

import com.dianping.puma.pumaserver.exception.PumaServerException;

public class BinlogChannelException extends PumaServerException {

	public BinlogChannelException(String msg) {
		super(msg);
	}

	public BinlogChannelException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
