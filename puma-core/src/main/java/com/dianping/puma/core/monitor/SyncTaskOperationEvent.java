package com.dianping.puma.core.monitor;

import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.constant.SyncType;

public class SyncTaskOperationEvent extends SyncTaskEvent {

	private SyncType syncType;

	private String taskName;

	private Operation operation;

	public SyncType getSyncType() {
		return syncType;
	}

	public void setSyncType(SyncType syncType) {
		this.syncType = syncType;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}
}
