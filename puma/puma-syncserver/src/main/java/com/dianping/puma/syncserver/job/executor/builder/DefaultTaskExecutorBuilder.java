package com.dianping.puma.syncserver.job.executor.builder;

import java.util.List;

import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

@SuppressWarnings("rawtypes")
public class DefaultTaskExecutorBuilder implements TaskExecutorBuilder {

    private List<TaskExecutorStrategy> strategys;

    @SuppressWarnings("unchecked")
    public TaskExecutor build(Task task) {
        Type type = task.getType();
        for (TaskExecutorStrategy strategy : strategys) {
            if (strategy.getType() == type) {
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
