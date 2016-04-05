package com.dianping.puma.alarm;

import com.dianping.puma.alarm.monitor.PumaAlarmMonitor;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.server.PumaServer;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmServer extends AbstractPumaLifeCycle implements PumaServer {

    private PumaAlarmMonitor monitor;

    @Override
    public void start() {
        super.start();

        monitor.start();
    }

    @Override
    public void stop() {
        super.stop();

        monitor.stop();
    }

    public void setMonitor(PumaAlarmMonitor monitor) {
        this.monitor = monitor;
    }
}
