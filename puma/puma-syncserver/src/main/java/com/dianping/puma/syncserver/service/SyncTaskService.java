package com.dianping.puma.syncserver.service;

import java.util.List;
import java.util.Map;

import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;

public interface SyncTaskService {

    /**
     * 根据状态和syncServerName查询
     */
    List<SyncTask> find(List<TaskState.State> states, String syncServerName);

    void updateState(long id, State state, Map<String, String> params);

}
