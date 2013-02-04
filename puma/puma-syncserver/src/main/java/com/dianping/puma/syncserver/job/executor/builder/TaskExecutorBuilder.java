package com.dianping.puma.syncserver.job.executor.builder;

import java.util.List;

import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

@SuppressWarnings("rawtypes")
public interface TaskExecutorBuilder {

    public TaskExecutor build(Task task);

    public void setStrategys(List<TaskExecutorStrategy> strategys);

}
