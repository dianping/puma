package com.dianping.puma.storage.index.biz;

import com.dianping.puma.core.model.BinlogInfo;

public class L2IndexKey {

	private BinlogInfo binlogInfo;

	public L2IndexKey(L1IndexKey l1IndexKey) {
		this.binlogInfo = l1IndexKey.getBinlogInfo();
	}

	public L2IndexKey(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
