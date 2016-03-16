package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.arbitrate.PumaAlarmArbiter;
import com.dianping.puma.alarm.notify.PumaAlarmNotifier;
import com.dianping.puma.alarm.service.ClientAlarmBenchmarkService;
import com.dianping.puma.alarm.service.ClientAlarmDataService;
import com.dianping.puma.alarm.service.ClientAlarmMetaService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.model.alarm.benchmark.AlarmBenchmark;
import com.dianping.puma.common.model.alarm.data.AlarmData;
import com.dianping.puma.common.model.alarm.result.AlarmResult;
import com.dianping.puma.common.service.ClientService;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class ScanningAlarmMonitor extends AbstractPumaLifeCycle implements PumaAlarmMonitor {

    private ClientService clientService;

    private ClientAlarmDataService clientAlarmDataService;

    private ClientAlarmBenchmarkService clientAlarmBenchmarkService;

    private ClientAlarmMetaService clientAlarmMetaService;

    private PumaAlarmArbiter arbiter;

    private PumaAlarmNotifier notifier;

    private void scan() {
        List<String> clientNames = clientService.findAllClientNames();
        for (String clientName: clientNames) {
            AlarmData data = clientAlarmDataService.findPullTimeDelay(clientName);
            AlarmBenchmark benchmark = clientAlarmBenchmarkService.findPullTimeDelay(clientName);
            AlarmResult result = arbiter.arbitrate(data, benchmark);
        }
    }
}
