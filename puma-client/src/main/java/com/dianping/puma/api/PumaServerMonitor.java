package com.dianping.puma.api;

import java.util.List;

public interface PumaServerMonitor {

    List<String> get();

    void addListener(PumaServerMonitorListener listener);

    void removeListener();

    interface PumaServerMonitorListener {
        void onChange(List<String> servers);
    }
}
