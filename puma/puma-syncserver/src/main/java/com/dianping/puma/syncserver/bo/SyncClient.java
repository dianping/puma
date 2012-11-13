package com.dianping.puma.syncserver.bo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.syncserver.web.SyncController;

public class SyncClient extends AbstractSyncClient {
    private static final Logger LOG = LoggerFactory.getLogger(SyncController.class);

    public SyncClient(SyncConfig sync) {
        super(sync, null);
        LOG.info("SyncClient inited.");
    }

    @Override
    protected void onEvent(ChangedEvent event) throws Exception {
        //执行同步
        mysqlExecutor.execute(event);
    }
}
