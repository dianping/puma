package com.dianping.puma.core.monitor;

import com.dianping.puma.core.replicate.model.task.StatusActionType;

public class ReplicationTaskStatusEvent extends ReplicationEvent {

	private StatusActionType statusActionType;

	private long taskId;
	
	private String taskName;

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

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}
}
