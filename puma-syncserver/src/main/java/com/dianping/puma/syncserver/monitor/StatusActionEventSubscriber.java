package com.dianping.puma.syncserver.monitor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.monitor.SwallowEventSubscriber;
import com.dianping.puma.syncserver.config.SyncServerConfig;

public class StatusActionEventSubscriber extends SwallowEventSubscriber {
    @Autowired
    private SyncServerConfig syncServerConfig;

    @PostConstruct
    public void init() {
        String type = syncServerConfig.getSyncServerName();
        super.setType(type);
        super.init();
    }

}
