package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.arbitrate.PumaAlarmArbiter;
import com.dianping.puma.alarm.regulate.PumaAlarmRegulator;
import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
import com.dianping.puma.alarm.notify.PumaAlarmNotifier;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.meta.AlarmMeta;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.common.service.ClientService;
import com.dianping.puma.common.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

    private PumaAlarmRegulator controller;

    private PumaAlarmNotifier notifier;

    private ClientService clientService;

    private List<PumaAlarmMonitor> monitors;

    private long scanIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("scanning-alarm-monitor-executor"));

    @Override
    public void start() {
        super.start();

        arbiter.start();
        controller.start();
        notifier.start();

        for (PumaAlarmMonitor monitor: monitors) {
            monitor.start();
        }

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

        for (PumaAlarmMonitor monitor: monitors) {
            monitor.stop();
        }

        notifier.stop();
        controller.stop();
        arbiter.stop();
    }

    private void scan() {
        try {
            List<String> clientNames = clientService.findAllClientNames();

            for (String clientName : clientNames) {
                for (PumaAlarmMonitor monitor : monitors) {
                    try {
                        AlarmData data = monitor.monitorData(clientName);
                        AlarmBenchmark benchmark = monitor.monitorBenchmark(clientName);
                        AlarmMeta meta = monitor.monitorMeta(clientName);
                        AlarmStrategy strategy = monitor.monitorStrategy(clientName);

                        AlarmResult result = arbiter.arbitrate(data, benchmark);
                        result = controller.regulate(clientName, result, strategy);
                        notifier.notify(result, meta);

                    } catch (Throwable t) {
                        logger.error("Failed to scan client[{}] alarm info.", clientName, t);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Failed to scan alarm info.", t);
        }
    }

    @Override
    public AlarmData monitorData(String clientName) throws PumaAlarmMonitorException {
        throw new PumaAlarmMonitorException("unsupported operation");
    }

    @Override
    public AlarmBenchmark monitorBenchmark(String clientName) throws PumaAlarmMonitorException {
        throw new PumaAlarmMonitorException("unsupported operation");
    }

    @Override
    public AlarmMeta monitorMeta(String clientName) throws PumaAlarmMonitorException {
        throw new PumaAlarmMonitorException("unsupported operation");
    }

    @Override
    public LinearAlarmStrategy monitorStrategy(String clientName) throws PumaAlarmMonitorException {
        throw new PumaAlarmMonitorException("unsupported operation");
    }

    public void setArbiter(PumaAlarmArbiter arbiter) {
        this.arbiter = arbiter;
    }

    public void setController(PumaAlarmRegulator controller) {
        this.controller = controller;
    }

    public void setNotifier(PumaAlarmNotifier notifier) {
        this.notifier = notifier;
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }

    public void setMonitors(List<PumaAlarmMonitor> monitors) {
        this.monitors = monitors;
    }

    public void setScanIntervalInSecond(long scanIntervalInSecond) {
        this.scanIntervalInSecond = scanIntervalInSecond;
    }
}
