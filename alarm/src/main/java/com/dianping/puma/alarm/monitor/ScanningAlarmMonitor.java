package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.arbitrate.PumaAlarmArbiter;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.meta.AlarmMeta;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.notify.PumaAlarmNotifier;
import com.dianping.puma.alarm.regulate.PumaAlarmRegulator;
import com.dianping.puma.alarm.service.ClientAlarmService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.service.ClientService;
import com.dianping.puma.common.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class ScanningAlarmMonitor extends AbstractPumaLifeCycle implements PumaAlarmMonitor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaAlarmArbiter arbiter;

    private PumaAlarmRegulator regulator;

    private PumaAlarmNotifier notifier;

    private ClientService clientService;

    private ClientAlarmService clientAlarmService;

    private long scanIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("scanning-alarm-monitor-executor"));

    @Override
    public void start() {
        super.start();

        arbiter.start();
        regulator.start();
        notifier.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    scan();
                } catch (Throwable t) {
                    logger.error("Failed to periodically scan alarm info.", t);
                }
            }
        }, 0, scanIntervalInSecond, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        super.stop();

        executor.shutdownNow();

        notifier.stop();
        regulator.stop();
        arbiter.stop();
    }

    private void scan() {
        try {
            List<String> clientNames = clientService.findAllClientNames();

            for (String clientName : clientNames) {
                Map<AlarmData, AlarmBenchmark> dataAndBenchmarks =
                        clientAlarmService.findDataAndBenchmark(clientName);
                AlarmStrategy strategy = clientAlarmService.findStrategy(clientName);
                List<AlarmMeta> metas = clientAlarmService.findMeta(clientName);

                for (Map.Entry<AlarmData, AlarmBenchmark> entry : dataAndBenchmarks.entrySet()) {
                    AlarmData data = entry.getKey();
                    AlarmBenchmark benchmark = entry.getValue();

                    AlarmResult result = arbiter.arbitrate(data, benchmark);
                    result = regulator.regulate(clientName, result, strategy);

                    for (AlarmMeta meta : metas) {
                        notifier.notify(result, meta);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Failed to scan alarm info.", t);
        }
    }

    public void setArbiter(PumaAlarmArbiter arbiter) {
        this.arbiter = arbiter;
    }

    public void setRegulator(PumaAlarmRegulator regulator) {
        this.regulator = regulator;
    }

    public void setNotifier(PumaAlarmNotifier notifier) {
        this.notifier = notifier;
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }

    public void setClientAlarmService(ClientAlarmService clientAlarmService) {
        this.clientAlarmService = clientAlarmService;
    }

    public void setScanIntervalInSecond(long scanIntervalInSecond) {
        this.scanIntervalInSecond = scanIntervalInSecond;
    }
}
