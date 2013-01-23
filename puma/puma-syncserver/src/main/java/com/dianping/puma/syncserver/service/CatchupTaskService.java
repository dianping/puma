package com.dianping.puma.syncserver.service;

import java.util.List;

import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.TaskState;

public interface CatchupTaskService {

    public List<CatchupTask> find(List<TaskState.State> states, String syncServerName);

}
