package com.dianping.puma.core.model.state;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;

public class SyncTaskState extends BaseSyncTaskState {

	private long delay;

	private Exception exception;

	private long inserts;

	private long updates;

	private long deletes;

	private long ddls;

	private Status status;

	private BinlogInfo binlogInfo;

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public long getInserts() {
		return inserts;
	}

	public void setInserts(long inserts) {
		this.inserts = inserts;
	}

	public long getUpdates() {
		return updates;
	}

	public void setUpdates(long updates) {
		this.updates = updates;
	}

	public long getDeletes() {
		return deletes;
	}

	public void setDeletes(long deletes) {
		this.deletes = deletes;
	}

	public long getDdls() {
		return ddls;
	}

	public void setDdls(long ddls) {
		this.ddls = ddls;
	}

	@Override public Status getStatus() {
		return status;
	}

	@Override public void setStatus(Status status) {
		this.status = status;
	}

	@Override public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	@Override public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
