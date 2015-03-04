package com.dianping.puma.core.model;

import com.dianping.puma.core.constant.Status;

public class PumaTaskState {

	private Status status;

	private BinlogInfo binlogInfo;

	private BinlogStat binlogStat;

	public PumaTaskState() {
		status = Status.WAITING;
		binlogInfo = new BinlogInfo();
		binlogStat = new BinlogStat();
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public BinlogStat getBinlogStat() {
		return binlogStat;
	}

	public void setBinlogStat(BinlogStat binlogStat) {
		this.binlogStat = binlogStat;
	}
}
