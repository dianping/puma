package com.dianping.puma.core.monitor.event;

import com.dianping.puma.core.model.state.TaskState;

import java.util.List;

public class TaskStateEvent<T extends TaskState> extends TaskEvent {

	private List<T> taskStates;

	public List<T> getTaskStates() {
		return taskStates;
	}

	public void setTaskStates(List<T> taskStates) {
		this.taskStates = taskStates;
	}
}
