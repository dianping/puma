package com.dianping.puma.core.monitor.event;

import com.dianping.puma.core.constant.ActionOperation;

public class TaskOperationEvent extends TaskEvent {

	private String taskName;

	private ActionOperation operation;

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
