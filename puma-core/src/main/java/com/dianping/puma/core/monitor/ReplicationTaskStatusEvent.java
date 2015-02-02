package com.dianping.puma.core.monitor;

import com.dianping.puma.core.replicate.model.task.StatusActionType;

public class ReplicationTaskStatusEvent {

	private StatusActionType statusActionType;

	private long taskId;

	public StatusActionType getStatusActionType() {
		return statusActionType;
	}

	public void setStatusActionType(StatusActionType statusActionType) {
		this.statusActionType = statusActionType;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
}
