package com.dianping.puma.syncserver.job.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.model.task.SyncTask;

public class SyncTaskExecutor extends AbstractTaskExecutor {
    protected static final Logger LOG = LoggerFactory.getLogger(SyncTaskExecutor.class);

    public SyncTaskExecutor(SyncTask syncTask, String pumaServerHost, int pumaServerPort, String target) {
        super(syncTask, pumaServerHost, pumaServerPort, target);
    }

    @Override
    protected void onEvent(ChangedEvent event) throws Exception {
        //执行同步
        mysqlExecutor.execute(event);
    }

}
