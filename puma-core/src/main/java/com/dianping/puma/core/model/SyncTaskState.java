package com.dianping.puma.core.model;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.constant.SyncType;

public class SyncTaskState extends TaskState {

	private String taskName;

	private SyncType syncType;

	private Status status;

	private BinlogInfo binlogInfo;

	private BinlogInfo binlogInfoOfIOThread;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public SyncType getSyncType() {
		return syncType;
	}

	public void setSyncType(SyncType syncType) {
		this.syncType = syncType;
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

	public BinlogInfo getBinlogInfoOfIOThread() {
		return binlogInfoOfIOThread;
	}

	public void setBinlogInfoOfIOThread(BinlogInfo binlogInfoOfIOThread) {
		this.binlogInfoOfIOThread = binlogInfoOfIOThread;
	}
}
