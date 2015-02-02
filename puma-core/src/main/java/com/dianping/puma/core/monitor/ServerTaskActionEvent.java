package com.dianping.puma.core.monitor;

import com.dianping.puma.core.server.model.ServerTaskActionStatus;

public class ServerTaskActionEvent extends Event {

	private long taskId;

	private ServerTaskActionStatus taskStatusAction;

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskStatusAction(ServerTaskActionStatus taskStatusAction) {
		this.taskStatusAction = taskStatusAction;
	}

	public ServerTaskActionStatus getTaskStatusAction() {
		return taskStatusAction;
	}

	@Override
	public String toString() {
		return "ServerTaskActionEvent [taskId=" + taskId + " taskStatusAction="
				+ taskStatusAction + "]";
	}
}
