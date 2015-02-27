package com.dianping.puma.core.monitor;

import com.dianping.puma.core.model.PumaTaskOperation;

public class PumaTaskOperationEvent extends PumaTaskEvent {

	private String taskId;

	private PumaTaskOperation operation;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public PumaTaskOperation getOperation() {
		return operation;
	}

	public void setOperation(PumaTaskOperation operation) {
		this.operation = operation;
	}
}
