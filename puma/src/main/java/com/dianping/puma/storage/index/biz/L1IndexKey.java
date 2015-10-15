package com.dianping.puma.storage.index.biz;

import com.dianping.puma.core.model.BinlogInfo;

public class L1IndexKey implements Comparable<L1IndexKey> {

	private boolean newBucket;

	private BinlogInfo binlogInfo;

	public L1IndexKey(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public L1IndexKey(boolean newBucket, BinlogInfo binlogInfo) {
		this.newBucket = newBucket;
		this.binlogInfo = binlogInfo;
	}

	@Override
	public int compareTo(L1IndexKey l1IndexKey) {
		return binlogInfo.compareTo(l1IndexKey.getBinlogInfo());
	}

	public boolean isNewBucket() {
		return newBucket;
	}

	public void setNewBucket(boolean newBucket) {
		this.newBucket = newBucket;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
