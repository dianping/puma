package com.dianping.puma.syncserver.job.executor.builder;

import java.util.List;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.biz.entity.old.BaseSyncTask;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

@SuppressWarnings("rawtypes")
public class DefaultTaskExecutorBuilder implements TaskExecutorBuilder {

    private List<TaskExecutorStrategy> strategys;

    @SuppressWarnings("unchecked")
    public TaskExecutor build(BaseSyncTask task) {
        SyncType syncType = task.getSyncType();
        for (TaskExecutorStrategy strategy : strategys) {
            if (strategy.getSyncType() == syncType) {
                return strategy.build(task);
            }
        }
        return null;
    }

    public List<TaskExecutorStrategy> getStrategys() {
        return strategys;
    }

    public void setStrategys(List<TaskExecutorStrategy> strategys) {
        this.strategys = strategys;
    }

}
