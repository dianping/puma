package com.dianping.puma.core.dto;

import com.dianping.puma.core.model.BinlogInfo;

public class BinlogAck {

	private BinlogInfo binlogInfo;

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
