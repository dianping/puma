package com.dianping.puma.core.model.state;

import java.util.List;
import java.util.Map;

public interface TaskStateContainer {

	public TaskState get(String taskName);

	public List<TaskState> getAll();

	public void add(String taskName, TaskState taskState);

	public void addAll(Map<String, TaskState> taskStates);

	public void remove(String taskName);

	public void removeAll();
}
