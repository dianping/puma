package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
import com.dianping.puma.alarm.service.ClientAlarmBenchmarkService;
import com.dianping.puma.alarm.service.ClientAlarmDataService;
import com.dianping.puma.alarm.service.ClientAlarmMetaService;
import com.dianping.puma.alarm.service.ClientAlarmStrategyService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.meta.AlarmMeta;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmMonitor extends AbstractPumaLifeCycle implements PumaAlarmMonitor {

    private ClientAlarmDataService dataService;

    private ClientAlarmBenchmarkService benchmarkService;

    private ClientAlarmMetaService metaService;

    private ClientAlarmStrategyService strategyService;

    @Override
    public AlarmData monitorData(String clientName) throws PumaAlarmMonitorException {
        return dataService.findPullTimeDelay(clientName);
    }

    @Override
    public AlarmBenchmark monitorBenchmark(String clientName) throws PumaAlarmMonitorException {
        return benchmarkService.findPullTimeDelay(clientName);
    }

    @Override
    public AlarmMeta monitorMeta(String clientName) throws PumaAlarmMonitorException {
        return metaService.find(clientName);
    }

    @Override
    public AlarmStrategy monitorStrategy(String clientName) throws PumaAlarmMonitorException {
        return strategyService.find(clientName);
    }

    public void setDataService(ClientAlarmDataService dataService) {
        this.dataService = dataService;
    }

    public void setBenchmarkService(ClientAlarmBenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    public void setMetaService(ClientAlarmMetaService metaService) {
        this.metaService = metaService;
    }

    public void setStrategyService(ClientAlarmStrategyService strategyService) {
        this.strategyService = strategyService;
    }
}
