package com.dianping.puma.core.monitor;

import com.dianping.puma.core.replicate.model.task.ActionType;

public class ReplicationTaskEvent extends Event {

	private ActionType actionType;

	private long taskId;

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
}
