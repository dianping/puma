package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.model.state.DumpTaskState;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.DumpTaskStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("dumpTaskStateService")
public class DumpTaskStateServiceImpl implements DumpTaskStateService {
	@Autowired
	TaskStateContainer taskStateContainer;

	@Override
	public DumpTaskState find(String taskName) {
		TaskState taskState = taskStateContainer.get(taskName);
		return (taskState instanceof DumpTaskState) ? (DumpTaskState) taskState : null;
	}

	@Override
	public List<DumpTaskState> findAll() {
		List<DumpTaskState> taskStates = new ArrayList<DumpTaskState>();
		for (Map.Entry<String, TaskState> taskStateEntry: taskStateContainer.getAll().entrySet()) {
			TaskState taskState = taskStateEntry.getValue();
			if (taskState instanceof DumpTaskState) {
				taskStates.add((DumpTaskState) taskState);
			}
		}
		return taskStates;
	}

	@Override
	public void add(DumpTaskState taskState) {
		taskStateContainer.add(taskState.getTaskName(), taskState);
	}

	@Override
	public void addAll(List<DumpTaskState> taskStates) {
		Map<String, TaskState> taskStateMap = new HashMap<String, TaskState>();
		for (TaskState taskState: taskStates) {
			taskStateMap.put(taskState.getTaskName(), taskState);
		}
		taskStateContainer.addAll(taskStateMap);
	}

	@Override
	public void remove(String taskName) {
		DumpTaskState taskState = find(taskName);
		if (taskState != null) {
			taskStateContainer.remove(taskState.getTaskName());
		}
	}

	@Override
	public void removeAll() {
		List<DumpTaskState> taskStates = findAll();
		for (DumpTaskState taskState: taskStates) {
			taskStateContainer.remove(taskState.getTaskName());
		}
	}
}
