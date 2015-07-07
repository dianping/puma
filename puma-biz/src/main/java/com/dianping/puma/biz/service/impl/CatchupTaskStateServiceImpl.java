package com.dianping.puma.biz.service.impl;

import com.dianping.puma.core.model.state.CatchupTaskState;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.biz.service.CatchupTaskStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("catchupTaskStateService")
public class CatchupTaskStateServiceImpl implements CatchupTaskStateService {
	@Autowired
	TaskStateContainer taskStateContainer;

	@Override
	public CatchupTaskState find(String taskName) {
		TaskState taskState = taskStateContainer.get(taskName);
		return (taskState instanceof CatchupTaskState) ? (CatchupTaskState) taskState : null;
	}

	@Override
	public List<CatchupTaskState> findAll() {
		List<CatchupTaskState> syncTaskStates = new ArrayList<CatchupTaskState>();
		List<TaskState> taskStates = taskStateContainer.getAll();
		for (TaskState taskState: taskStates) {
			if (taskState instanceof CatchupTaskState) {
				syncTaskStates.add((CatchupTaskState) taskState);
			}
		}
		return syncTaskStates;
	}

	@Override
	public void add(CatchupTaskState taskState) {
		taskStateContainer.add(taskState.getTaskName(), taskState);
	}

	@Override
	public void addAll(List<CatchupTaskState> taskStates) {
		Map<String, TaskState> taskStateMap = new HashMap<String, TaskState>();
		for (TaskState taskState: taskStates) {
			taskStateMap.put(taskState.getTaskName(), taskState);
		}
		taskStateContainer.addAll(taskStateMap);
	}

	@Override
	public void remove(String taskName) {
		CatchupTaskState taskState = find(taskName);
		if (taskState != null) {
			taskStateContainer.remove(taskState.getTaskName());
		}
	}

	@Override
	public void removeAll() {
		List<CatchupTaskState> taskStates = findAll();
		for (CatchupTaskState taskState: taskStates) {
			taskStateContainer.remove(taskState.getTaskName());
		}
	}
}
