package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaServerMonitor;
import com.dianping.puma.api.PumaServerRouter;

import java.util.List;

public class RoundRobinPumaServerRouter implements PumaServerRouter {

    protected PumaServerMonitor monitor;

    private boolean inited = false;

    private volatile List<String> servers;

    private int index;

    public RoundRobinPumaServerRouter(PumaServerMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public String next() {
        if (!inited) {
            init();
            inited = true;
        }

        if (servers == null || servers.size() == 0) {
            return null;
        }

        if (index >= servers.size()) {
            index = index - servers.size();
        }

        return servers.get(index++);
    }

    @Override
    public boolean exist(String server) {
        if (!inited) {
            init();
            inited = true;
        }

        return servers != null && servers.contains(server);
    }

    protected void init() {
        servers = monitor.get();
        PumaServerMonitor.PumaServerMonitorListener listener = new PumaServerMonitor.PumaServerMonitorListener() {
            @Override
            public void onChange(List<String> servers) {
                RoundRobinPumaServerRouter.this.servers = servers;
            }
        };
        monitor.addListener(listener);
    }
}
