package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.service.SyncTaskStateService;
import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("SyncTaskStateService")
public class SyncTaskStateServiceImpl implements SyncTaskStateService {

	@Autowired
	TaskStateContainer taskStateContainer;

	@Override
	public SyncTaskState find(String taskName) {
		TaskState taskState = taskStateContainer.get(taskName);
		return (taskState instanceof SyncTaskState) ? (SyncTaskState) taskState : null;
	}

	@Override
	public List<SyncTaskState> findAll() {
		List<SyncTaskState> syncTaskStates = new ArrayList<SyncTaskState>();
		List<TaskState> taskStates = taskStateContainer.getAll();
		for (TaskState taskState: taskStates) {
			if (taskState instanceof SyncTaskState) {
				syncTaskStates.add((SyncTaskState) taskState);
			}
		}
		return syncTaskStates;
	}

	@Override
	public void add(SyncTaskState taskState) {
		taskStateContainer.add(taskState.getTaskName(), taskState);
	}

	@Override
	public void addAll(List<SyncTaskState> taskStates) {
		Map<String, TaskState> taskStateMap = new HashMap<String, TaskState>();
		for (TaskState taskState: taskStates) {
			taskStateMap.put(taskState.getTaskName(), taskState);
		}
		taskStateContainer.addAll(taskStateMap);
	}

	@Override
	public void remove(String taskName) {
		SyncTaskState taskState = find(taskName);
		if (taskState != null) {
			taskStateContainer.remove(taskState.getTaskName());
		}
	}

	@Override
	public void removeAll() {
		List<SyncTaskState> taskStates = findAll();
		for (SyncTaskState taskState: taskStates) {
			taskStateContainer.remove(taskState.getTaskName());
		}
	}
}
