package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.ShardSyncTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("shardSyncTaskExecutorStrategy")
public class ShardSyncTaskExecutorStrategy implements TaskExecutorStrategy<ShardSyncTask, ShardSyncTaskExecutor> {

    @Autowired
    private PumaServerService pumaServerService;

    @Autowired
    private PumaTaskService pumaTaskService;

    @Autowired
    private SrcDBInstanceService srcDBInstanceService;

    @Override
    public ShardSyncTaskExecutor build(ShardSyncTask task) {
        ShardSyncTaskExecutor executor = new ShardSyncTaskExecutor(task);
        executor.setPumaServerService(pumaServerService);
        executor.setSrcDBInstanceService(srcDBInstanceService);
        executor.setPumaTaskService(pumaTaskService);
        executor.init();
        return executor;
    }

    @Override
    public Type getType() {
        return Type.SHARD_SYNC;
    }
}
