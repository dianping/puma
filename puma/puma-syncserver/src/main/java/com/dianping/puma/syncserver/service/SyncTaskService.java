package com.dianping.puma.syncserver.service;

import java.util.List;

import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState;

public interface SyncTaskService {

    /**
     * 根据状态和syncServerName查询
     */
    List<SyncTask> find(List<TaskState.State> states, String syncServerName);

}
