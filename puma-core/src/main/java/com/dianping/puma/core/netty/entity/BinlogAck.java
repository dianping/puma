package com.dianping.puma.core.netty.entity;

import com.dianping.puma.core.model.BinlogInfo;

public class BinlogAck {

	private String clientName;

	private BinlogInfo binlogInfo;

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
