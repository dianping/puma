package com.dianping.puma.core.model.state.storage;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.state.State;

public class StorageState extends State {

	private String taskName;

	private long seq;

	private BinlogInfo binlogInfo;

	public StorageState(String name, String taskName, BinlogInfo binlogInfo) {
		super(name);
		this.taskName = taskName;
		this.binlogInfo = binlogInfo;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
