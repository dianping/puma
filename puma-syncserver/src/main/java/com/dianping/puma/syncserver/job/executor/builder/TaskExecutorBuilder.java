package com.dianping.puma.syncserver.job.executor.builder;

import java.util.List;

import com.dianping.puma.biz.entity.BaseSyncTask;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

@SuppressWarnings("rawtypes")
public interface TaskExecutorBuilder {

    public TaskExecutor build(BaseSyncTask task);

    public void setStrategys(List<TaskExecutorStrategy> strategys);

}
