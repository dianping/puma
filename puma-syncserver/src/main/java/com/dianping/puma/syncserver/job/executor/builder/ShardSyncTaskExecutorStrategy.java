package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.ShardSyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service("shardSyncTaskExecutorStrategy")
public class ShardSyncTaskExecutorStrategy implements TaskExecutorStrategy<ShardSyncTask, ShardSyncTaskExecutor> {
    @Override
    public ShardSyncTaskExecutor build(ShardSyncTask task) {
        return null;
    }

    @Override
    public Type getType() {
        return Type.SHARD_SYNC;
    }
}
