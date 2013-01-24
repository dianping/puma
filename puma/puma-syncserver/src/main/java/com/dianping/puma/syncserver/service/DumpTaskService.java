package com.dianping.puma.syncserver.service;

import java.util.List;
import java.util.Map;

import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;

public interface DumpTaskService {

    public List<DumpTask> find(List<TaskState.State> states, String syncServerName);

    void updateState(long id, State state, Map<String, String> params);
}
