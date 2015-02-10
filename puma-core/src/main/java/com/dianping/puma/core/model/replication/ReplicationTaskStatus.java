package com.dianping.puma.core.model.replication;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TaskStatus;

public class ReplicationTaskStatus extends TaskStatus {

	private Status status;

	private BinlogInfo binlogInfo;

	private long rowsInsert;

	private long rowsDelete;

	private long rowsUpdate;

	private long ddls;

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

	public long getRowsInsert() {
		return rowsInsert;
	}

	public void setRowsInsert(long rowsInsert) {
		this.rowsInsert = rowsInsert;
	}

	public long getRowsDelete() {
		return rowsDelete;
	}

	public void setRowsDelete(long rowsDelete) {
		this.rowsDelete = rowsDelete;
	}

	public long getRowsUpdate() {
		return rowsUpdate;
	}

	public void setRowsUpdate(long rowsUpdate) {
		this.rowsUpdate = rowsUpdate;
	}

	public long getDdls() {
		return ddls;
	}

	public void setDdls(long ddls) {
		this.ddls = ddls;
	}

	public enum Status {
		WAITING,
		PREPARING,
		RUNNING,
		STOPPING,
		STOPPED,
		FAILED,
		ERROR
	}
}
