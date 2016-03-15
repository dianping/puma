package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
import com.dianping.puma.alarm.model.benchmark.PumaAlarmBenchmark;
import com.dianping.puma.alarm.model.raw.PumaAlarmRawData;
import com.dianping.puma.alarm.service.ClientAlarmBenchmarkService;
import com.dianping.puma.alarm.service.ClientAlarmMetaService;
import com.dianping.puma.biz.service.ClientAlarmDataService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.service.ClientService;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PolledAlarmMonitor extends AbstractPumaLifeCycle implements PumaAlarmMonitor {

    private ClientService clientService;

    private ClientAlarmMetaService clientAlarmMetaService;

    private ClientAlarmDataService clientAlarmDataService;

    private ClientAlarmBenchmarkService clientAlarmBenchmarkService;

    private void scan() {
    }

    @Override
    public PumaAlarmBenchmark getBenchmark(String clientName) throws PumaAlarmMonitorException {
        return null;
    }

    @Override
    public PumaAlarmRawData getRawData(String clientName) throws PumaAlarmMonitorException {
        return null;
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }

    public void setClientAlarmMetaService(ClientAlarmMetaService clientAlarmMetaService) {
        this.clientAlarmMetaService = clientAlarmMetaService;
    }

    public void setClientAlarmDataService(ClientAlarmDataService clientAlarmDataService) {
        this.clientAlarmDataService = clientAlarmDataService;
    }

    public void setClientAlarmBenchmarkService(ClientAlarmBenchmarkService clientAlarmBenchmarkService) {
        this.clientAlarmBenchmarkService = clientAlarmBenchmarkService;
    }
}
