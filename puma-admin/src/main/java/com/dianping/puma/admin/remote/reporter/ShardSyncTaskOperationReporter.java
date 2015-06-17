package com.dianping.puma.admin.remote.reporter;

import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.ShardSyncTaskOperationEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("shardSyncTaskOperationReporter")
public class ShardSyncTaskOperationReporter {

    @Autowired
    SwallowEventPublisher shardSyncTaskOperationEventPublisher;

    public void report(String syncServerName, String taskName, SyncType syncType, ActionOperation operation) throws SendFailedException {
        ShardSyncTaskOperationEvent event = new ShardSyncTaskOperationEvent();
        event.setServerName(syncServerName);
        event.setTaskName(taskName);
        event.setOperation(operation);
        event.setSyncType(syncType);
        shardSyncTaskOperationEventPublisher.publish(event);
    }
}
