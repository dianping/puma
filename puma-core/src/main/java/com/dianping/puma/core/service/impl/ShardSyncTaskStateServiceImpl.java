package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.model.state.ShardSyncTaskState;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.ShardSyncTaskStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("shardSyncTaskStateService")
public class ShardSyncTaskStateServiceImpl implements ShardSyncTaskStateService {

    @Autowired
    TaskStateContainer taskStateContainer;

    @Override
    public ShardSyncTaskState find(String taskName) {
        TaskState taskState = taskStateContainer.get(taskName);
        return (taskState instanceof ShardSyncTaskState) ? (ShardSyncTaskState) taskState : null;
    }

    @Override
    public List<ShardSyncTaskState> findAll() {
        List<ShardSyncTaskState> syncTaskStates = new ArrayList<ShardSyncTaskState>();
        List<TaskState> taskStates = taskStateContainer.getAll();
        for (TaskState taskState : taskStates) {
            if (taskState instanceof ShardSyncTaskState) {
                syncTaskStates.add((ShardSyncTaskState) taskState);
            }
        }
        return syncTaskStates;
    }

    @Override
    public void add(ShardSyncTaskState taskState) {
        taskStateContainer.add(taskState.getTaskName(), taskState);
    }

    @Override
    public void addAll(List<ShardSyncTaskState> taskStates) {
        Map<String, TaskState> taskStateMap = new HashMap<String, TaskState>();
        for (TaskState taskState : taskStates) {
            taskStateMap.put(taskState.getTaskName(), taskState);
        }
        taskStateContainer.addAll(taskStateMap);
    }

    @Override
    public void remove(String taskName) {
        ShardSyncTaskState taskState = find(taskName);
        if (taskState != null) {
            taskStateContainer.remove(taskState.getTaskName());
        }
    }

    @Override
    public void removeAll() {
        List<ShardSyncTaskState> taskStates = findAll();
        for (ShardSyncTaskState taskState : taskStates) {
            taskStateContainer.remove(taskState.getTaskName());
        }
    }
}
