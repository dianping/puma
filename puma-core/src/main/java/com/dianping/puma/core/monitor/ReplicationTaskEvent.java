package com.dianping.puma.core.monitor;

import com.dianping.puma.core.replicate.model.task.ActionType;

public class ReplicationTaskEvent extends ReplicationEvent {

	private ActionType actionType;

	private String taskId;

	private String taskName;
	
	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}
}
