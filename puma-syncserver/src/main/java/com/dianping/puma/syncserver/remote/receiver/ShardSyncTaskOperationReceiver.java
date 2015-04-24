package com.dianping.puma.syncserver.remote.receiver;

import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.ShardSyncTask;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.event.ShardSyncTaskOperationEvent;
import com.dianping.puma.core.service.ShardDumpTaskService;
import com.dianping.puma.core.service.ShardSyncTaskService;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.job.executor.builder.TaskExecutorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("shardSyncTaskOperationReceiver")
public class ShardSyncTaskOperationReceiver implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ShardSyncTaskOperationReceiver.class);

    @Autowired
    ShardSyncTaskService shardSyncTaskService;

    @Autowired
    ShardDumpTaskService shardDumpTaskService;

    @Autowired
    TaskExecutorBuilder taskExecutorBuilder;

    @Autowired
    TaskExecutorContainer taskExecutorContainer;

    @Autowired
    NotifyService notifyService;

    @Override
    public void onEvent(Event event) {
        if (event instanceof ShardSyncTaskOperationEvent) {
            LOG.info("Receive shard sync task operation event.");

            ShardSyncTaskOperationEvent shardEvent = (ShardSyncTaskOperationEvent) event;
            String taskName = shardEvent.getTaskName();
            ActionOperation operation = ((ShardSyncTaskOperationEvent) event).getOperation();

            switch (operation) {
                case CREATE:
                    TaskExecutor taskExecutor = null;

                    if (shardEvent.getSyncType().equals(SyncType.SHARD_SYNC)) {
                        ShardSyncTask syncTask = shardSyncTaskService.find(taskName);
                        taskExecutor = taskExecutorBuilder.build(syncTask);
                    } else if (shardEvent.getSyncType().equals(SyncType.SHARD_DUMP)) {
                        ShardDumpTask dumpTask = shardDumpTaskService.find(taskName);
                        taskExecutor = taskExecutorBuilder.build(dumpTask);
                    }

                    if (taskExecutor != null) {
                        try {
                            taskExecutorContainer.submit(taskExecutor);
                        } catch (TaskExecutionException e) {
                            notifyService.alarm(e.getMessage(), e, false);
                        }
                    }

                    break;
                case REMOVE:
                    if (shardEvent.getSyncType().equals(SyncType.SHARD_SYNC)) {
                        taskExecutorContainer.deleteShardSyncTask(taskName);
                    } else if (shardEvent.getSyncType().equals(SyncType.SHARD_DUMP)) {
                        taskExecutorContainer.deleteShardDumpTask(taskName);
                    }
            }
        }
    }
}
