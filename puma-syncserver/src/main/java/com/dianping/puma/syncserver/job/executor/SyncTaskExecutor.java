package com.dianping.puma.syncserver.job.executor;

import java.sql.SQLException;

import com.dianping.puma.core.entity.SyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;

public class SyncTaskExecutor extends AbstractTaskExecutor<SyncTask> {
    protected static final Logger LOG = LoggerFactory.getLogger(SyncTaskExecutor.class);

    public SyncTaskExecutor(SyncTask syncTask, String pumaServerHost, int pumaServerPort, String target) {
        super(syncTask, pumaServerHost, pumaServerPort, target);
    }

    @Override
    protected void execute(ChangedEvent event) throws SQLException {
        //执行同步
        mysqlExecutor.execute(event);
    }

}
