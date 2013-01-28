package com.dianping.puma.syncserver.monitor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.monitor.SwallowEventSubscriber;
import com.dianping.puma.syncserver.conf.Config;

public class TaskEventSubscriber extends SwallowEventSubscriber {
    @Autowired
    private Config config;

    @PostConstruct
    public void init() {
        String type = config.getSyncServerName();
        super.setType(type);
    }

}
