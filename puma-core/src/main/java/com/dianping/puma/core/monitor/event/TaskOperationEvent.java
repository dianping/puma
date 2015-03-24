package com.dianping.puma.core.monitor.event;

import com.dianping.puma.core.monitor.event.TaskEvent;

public class TaskOperationEvent extends TaskEvent {

	private String taskName;

	private ActionOperation operation;

	public String getTaskName() {
		return taskName;
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
