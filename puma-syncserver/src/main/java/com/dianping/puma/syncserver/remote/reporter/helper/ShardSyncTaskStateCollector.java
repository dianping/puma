package com.dianping.puma.syncserver.remote.reporter.helper;

import com.dianping.puma.core.model.state.ShardSyncTaskState;
import com.dianping.puma.core.service.ShardSyncTaskStateService;
import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.ShardDumpTaskExecutor;
import com.dianping.puma.syncserver.job.executor.ShardSyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("shardSyncTaskStateCollector")
public class ShardSyncTaskStateCollector {

    @Autowired
    ShardSyncTaskStateService shardSyncTaskStateService;

    @Autowired
    SyncTaskService syncTaskService;

    @Autowired
    TaskExecutorContainer taskExecutorContainer;

    @Scheduled(cron = "0/5 * * * * ?")
    public void collect() {
        shardSyncTaskStateService.removeAll();

        for (TaskExecutor taskExecutor : taskExecutorContainer.getAll()) {
            if (taskExecutor instanceof ShardSyncTaskExecutor) {
                ShardSyncTaskState shardSyncTaskState = ((ShardSyncTaskExecutor) taskExecutor).getTaskState();
                shardSyncTaskState.setGmtUpdate(new Date());
                shardSyncTaskStateService.add(shardSyncTaskState);
            } else if (taskExecutor instanceof ShardDumpTaskExecutor) {
                ShardSyncTaskState shardSyncTaskState = ((ShardDumpTaskExecutor) taskExecutor).getTaskState();
                shardSyncTaskState.setGmtUpdate(new Date());
                shardSyncTaskStateService.add(shardSyncTaskState);
            }
        }
    }
}
