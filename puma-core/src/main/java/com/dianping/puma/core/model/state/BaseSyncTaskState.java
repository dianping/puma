package com.dianping.puma.core.model.state;

import com.dianping.puma.core.model.BinlogInfo;

public class BaseSyncTaskState extends TaskState {
	private BinlogInfo binlogInfoOfIOThread;

	public BinlogInfo getBinlogInfoOfIOThread() {
		return binlogInfoOfIOThread;
	}

	public void setBinlogInfoOfIOThread(BinlogInfo binlogInfoOfIOThread) {
		this.binlogInfoOfIOThread = binlogInfoOfIOThread;
	}
}
