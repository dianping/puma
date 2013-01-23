package com.dianping.puma.syncserver.service;

import java.util.List;

import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.TaskState;

public interface DumpTaskService {

    public List<DumpTask> find(List<TaskState.State> states, String syncServerName);
}
