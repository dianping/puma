package com.dianping.puma.core.model.state;

public class SyncTaskState extends BaseSyncTaskState {

	private long delay;

	private Exception exception;

	private long inserts;

	private long updates;

	private long deletes;

	private long ddls;

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
}
