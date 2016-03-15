package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
import com.dianping.puma.alarm.model.benchmark.PumaAlarmBenchmark;
import com.dianping.puma.alarm.model.raw.PumaAlarmRawData;
import com.dianping.puma.biz.service.ClientAlarmDataService;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeAlarmMonitor extends AbstractPumaLifeCycle implements PumaAlarmMonitor {

    private ClientAlarmDataService clientAlarmDataService;

    @Override
    public PumaAlarmBenchmark getBenchmark(String clientName) throws PumaAlarmMonitorException {
        return null;
    }

    @Override
    public PumaAlarmRawData getRawData(String clientName) throws PumaAlarmMonitorException {
        return null;
    }

    public void setClientAlarmDataService(ClientAlarmDataService clientAlarmDataService) {
        this.clientAlarmDataService = clientAlarmDataService;
    }
}
