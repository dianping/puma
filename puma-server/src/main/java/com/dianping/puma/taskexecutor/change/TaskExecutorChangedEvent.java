package com.dianping.puma.taskexecutor.change;

public class TaskExecutorChangedEvent {

	protected String taskName;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
