package com.dianping.puma.core.netty.entity.binlog.response;

import com.dianping.puma.core.netty.entity.BinlogMessage;

public class BinlogGetResponse extends BinlogResponse {

	private BinlogMessage binlogMessage;

	public void setBinlogMessage(BinlogMessage binlogMessage) {
		this.binlogMessage = binlogMessage;
	}
}
