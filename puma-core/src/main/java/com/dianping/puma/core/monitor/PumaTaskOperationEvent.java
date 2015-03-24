package com.dianping.puma.core.monitor;

import com.dianping.puma.core.constant.ActionOperation;

public class PumaTaskOperationEvent extends PumaTaskEvent {

	private String taskId;
	
	private String taskName;

	private ActionOperation operation;

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

	public ActionOperation getOperation() {
		return operation;
	}

	public void setOperation(ActionOperation operation) {
		this.operation = operation;
	}
}
