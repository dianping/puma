package com.dianping.puma.syncserver.web;

import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.Sync;

public class SyncClient {
    private Sync sync;
    private PumaClient pumaClient;

    public Sync getSync() {
        return sync;
    }

    public void setSync(Sync sync) {
        this.sync = sync;
    }

    public PumaClient getPumaClient() {
        return pumaClient;
    }

    public void start() {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.ddl(true);
        configBuilder.dml(true);
        configBuilder.host("10.1.77.46");
        configBuilder.port(7862);
        configBuilder.serverId(1111);
        configBuilder.name("testClient");
        configBuilder.tables("DianPing", "*");
        configBuilder.target("77_21");
        configBuilder.transaction(true);
        pumaClient = new PumaClient(configBuilder.build());
        pumaClient.register(new EventListener() {

            @Override
            public void onSkipEvent(ChangedEvent event) {
                System.out.println(">>>>>>>>>>>>>>>>>>Skip " + event);
            }

            @Override
            public boolean onException(ChangedEvent event, Exception e) {
                System.out.println("-------------Exception " + e);
                return true;
            }

            @Override
            public void onEvent(ChangedEvent event) throws Exception {
                System.out.println("********************Received " + event);
            }
        });
        pumaClient.start();
    }
}
