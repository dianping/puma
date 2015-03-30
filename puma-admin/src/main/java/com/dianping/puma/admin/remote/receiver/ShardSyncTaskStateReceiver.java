package com.dianping.puma.admin.remote.receiver;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.ShardSyncTask;
import com.dianping.puma.core.model.state.ShardSyncTaskState;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.event.ShardSyncTaskStateEvent;
import com.dianping.puma.core.service.ShardSyncTaskService;
import com.dianping.puma.core.service.ShardSyncTaskStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service("shardSyncTaskStateReceiver")
public class ShardSyncTaskStateReceiver implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(ShardSyncTaskStateReceiver.class);

    @Autowired
    ShardSyncTaskStateService shardSyncTaskStateService;

    @Autowired
    ShardSyncTaskService shardSyncTaskService;

    @PostConstruct
    public void init() {
        List<ShardSyncTask> syncTasks = shardSyncTaskService.findAll();
        for (ShardSyncTask shardSyncTask : syncTasks) {
            ShardSyncTaskState syncTaskState = new ShardSyncTaskState();
            syncTaskState.setTaskName(shardSyncTask.getName());
            syncTaskState.setStatus(Status.PREPARING);
            shardSyncTaskStateService.add(syncTaskState);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ShardSyncTaskStateEvent) {
            LOG.info("Receive shard sync task state event.");

            List<ShardSyncTaskState> syncTaskStates = ((ShardSyncTaskStateEvent) event).getTaskStates();
            for (ShardSyncTaskState syncTaskState : syncTaskStates) {
                shardSyncTaskStateService.add(syncTaskState);
            }
        }
    }
}
