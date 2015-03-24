package com.dianping.puma.core.monitor.event;

import com.dianping.puma.core.model.state.TaskState;

import java.util.Map;

public class TaskStateEvent<T extends TaskState> extends TaskEvent {

	private Map<String, T> taskStates;

	public Map<String, T> getTaskStates() {
		return taskStates;
	}

	public void setTaskStates(Map<String, T> taskStates) {
		this.taskStates = taskStates;
	}
}
