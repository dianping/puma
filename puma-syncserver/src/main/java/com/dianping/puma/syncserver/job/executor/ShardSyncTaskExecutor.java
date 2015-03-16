package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardSyncTaskExecutor implements TaskExecutor<ShardSyncTask> {
    private final ShardSyncTask task;

    public ShardSyncTaskExecutor(ShardSyncTask task) {
        this.task = task;
    }

    @Override
    public void start() {

    }

    @Override
    public void pause(String detail) {

    }

    @Override
    public void succeed() {

    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return null;
    }

    @Override
    public ShardSyncTask getTask() {
        return task;
    }

    @Override
    public void stop(String detail) {

    }
}
