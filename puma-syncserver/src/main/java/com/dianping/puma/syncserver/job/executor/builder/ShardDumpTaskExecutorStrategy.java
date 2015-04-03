package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.service.DstDBInstanceService;
import com.dianping.puma.core.service.ShardDumpTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.ShardDumpTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("shardDumpTaskExecutorStrategy")
public class ShardDumpTaskExecutorStrategy implements TaskExecutorStrategy<ShardDumpTask, ShardDumpTaskExecutor> {

    @Autowired
    private SrcDBInstanceService srcDBInstanceService;

    @Autowired
    private DstDBInstanceService dstDBInstanceService;

    @Autowired
    private ShardDumpTaskService shardDumpTaskService;

    @Override
    public ShardDumpTaskExecutor build(ShardDumpTask task) {
        ShardDumpTaskExecutor executor = new ShardDumpTaskExecutor(task);
        executor.setSrcDBInstance(srcDBInstanceService.find(task.getSrcDbName()));
        executor.setDstDBInstance(dstDBInstanceService.find(task.getDstDbName()));
        executor.setShardDumpTaskService(shardDumpTaskService);
        executor.init();
        return executor;
    }

    @Override
    public Type getType() {
        return Type.SHARD_DUMP;
    }

    @Override
    public SyncType getSyncType() {
        return SyncType.SHARD_DUMP;
    }
}
