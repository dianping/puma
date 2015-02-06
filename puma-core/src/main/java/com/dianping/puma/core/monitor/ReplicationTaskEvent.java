package com.dianping.puma.core.monitor;

import com.dianping.puma.core.replicate.model.task.ActionType;

public class ReplicationTaskEvent extends ReplicationEvent {

	private ActionType actionType;

	private long taskId;

	private String taskName;
	
	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
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
