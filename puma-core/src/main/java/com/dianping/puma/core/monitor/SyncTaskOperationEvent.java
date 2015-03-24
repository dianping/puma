package com.dianping.puma.core.monitor;

import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.constant.SyncType;

public class SyncTaskOperationEvent extends SyncTaskEvent {

	private SyncType syncType;

	private String taskName;

	private ActionOperation operation;

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

	public ActionOperation getOperation() {
		return operation;
	}

	public void setOperation(ActionOperation operation) {
		this.operation = operation;
	}
}
