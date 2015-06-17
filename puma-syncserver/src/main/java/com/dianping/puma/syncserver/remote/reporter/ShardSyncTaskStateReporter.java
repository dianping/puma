package com.dianping.puma.syncserver.remote.reporter;

import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.ShardSyncTaskStateEvent;
import com.dianping.puma.core.service.ShardSyncTaskStateService;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("shardSyncTaskStateReporter")
public class ShardSyncTaskStateReporter {

    @Autowired
    SwallowEventPublisher shardSyncTaskStatePublisher;

    @Autowired
    ShardSyncTaskStateService shardSyncTaskStateService;

    @Autowired
    SyncServerConfig syncServerConfig;

    @Scheduled(cron = "0/5 * * * * ?")
    public void report() throws SendFailedException {
        ShardSyncTaskStateEvent event = new ShardSyncTaskStateEvent();
        event.setServerName(syncServerConfig.getSyncServerName());
        event.setTaskStates(shardSyncTaskStateService.findAll());
        shardSyncTaskStatePublisher.publish(event);
    }
}
