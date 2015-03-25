package com.dianping.puma.core.model.state;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("taskStateContainer")
public class DefaultTaskStateContainer implements TaskStateContainer {

	private Map<String, TaskState> taskStates = new ConcurrentHashMap<String, TaskState>();

	@Override
	public TaskState get(String taskName) {
		return taskStates.get(taskName);
	}

	@Override
	public List<TaskState> getAll() {
		return new ArrayList<TaskState>(taskStates.values());
	}

	@Override
	public void add(String taskName, TaskState taskState) {
		taskStates.put(taskName, taskState);
	}

	@Override
	public void addAll(Map<String, TaskState> taskStates) {
		this.taskStates.putAll(taskStates);
	}

	@Override
	public void remove(String taskName) {
		taskStates.remove(taskName);
	}

	@Override
	public void removeAll() {
		taskStates.clear();
	}
}
