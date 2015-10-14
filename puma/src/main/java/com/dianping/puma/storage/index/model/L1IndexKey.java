package com.dianping.puma.storage.index.model;

import com.dianping.puma.core.model.BinlogInfo;

public class L1IndexKey {

	private BinlogInfo binlogInfo;

	public L1IndexKey(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
