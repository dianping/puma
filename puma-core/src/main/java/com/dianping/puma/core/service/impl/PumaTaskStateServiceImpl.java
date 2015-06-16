package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.PumaTaskStateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("pumaTaskStateService")
public class PumaTaskStateServiceImpl implements PumaTaskStateService {

	private static final String NAME_INFIX = "_";

	@Autowired
	TaskStateContainer taskStateContainer;
	
	public void add(PumaTaskState taskState) {
		taskStateContainer.add(taskState.getName(), taskState);
	}

	public void addAll(List<PumaTaskState> taskStates) {
		Map<String, TaskState> taskStateMap = new HashMap<String, TaskState>();
		for (TaskState taskState: taskStates) {
			taskStateMap.put(taskState.getName(), taskState);
		}
		taskStateContainer.addAll(taskStateMap);
	}

	
	public PumaTaskState find(String name) {
		TaskState taskState = taskStateContainer.get(name);
		return (taskState instanceof PumaTaskState) ? (PumaTaskState) taskState : null;
	}

	public List<PumaTaskState> findAll() {
		List<PumaTaskState> syncTaskStates = new ArrayList<PumaTaskState>();
		List<TaskState> taskStates = taskStateContainer.getAll();
		for (TaskState taskState: taskStates) {
			if (taskState instanceof PumaTaskState) {
				syncTaskStates.add((PumaTaskState) taskState);
			}
		}
		return syncTaskStates;
	}

	public void remove(String name) {
		PumaTaskState taskState = find(name);
		if (taskState != null) {
			taskStateContainer.remove(taskState.getTaskName());
		}
	}

	public void removeAll() {
		List<PumaTaskState> taskStates = findAll();
		for (PumaTaskState taskState: taskStates) {
			taskStateContainer.remove(taskState.getTaskName());
		}
	}

	public String getStateName(String taskName, String serverName) {
		return taskName + NAME_INFIX + serverName;
	}

}
