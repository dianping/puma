package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.arbitrate.PumaAlarmArbiter;
import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.model.meta.*;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.alarm.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.model.strategy.NoAlarmStrategy;
import com.dianping.puma.alarm.notify.PumaAlarmNotifier;
import com.dianping.puma.alarm.regulate.PumaAlarmRegulator;
import com.dianping.puma.alarm.service.PumaClientAlarmBenchmarkService;
import com.dianping.puma.alarm.service.PumaClientAlarmDataService;
import com.dianping.puma.alarm.service.PumaClientAlarmMetaService;
import com.dianping.puma.alarm.service.PumaClientAlarmStrategyService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.service.PumaClientService;
import com.dianping.puma.common.utils.NamedThreadFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

    private PumaClientService pumaClientService;

    private PumaClientAlarmDataService pumaClientAlarmDataService;

    private PumaClientAlarmBenchmarkService pumaClientAlarmBenchmarkService;

    private PumaClientAlarmStrategyService pumaClientAlarmStrategyService;

    private PumaClientAlarmMetaService pumaClientAlarmMetaService;

    private long scanIntervalInSecond = 10;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("scanning-alarm-monitor-executor", false));

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
                    if (logger.isDebugEnabled()) {
                        logger.debug("Start periodically scanning puma alarms...");
                    }

                    scan();
                } catch (Throwable t) {
                    logger.error("Failed to periodically scan puma alarms.", t);
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
        List<String> clientNames = pumaClientService.findAllClientNames();

        for (String clientName : clientNames) {
            AlarmStrategy strategy = monitorStrategy(clientName);
            List<AlarmMeta> metas = monitorMetas(clientName);

            Map<AlarmData, AlarmBenchmark> map = Maps.newHashMap();

            PullTimeDelayAlarmData pullTimeDelayAlarmData
                    = monitorPullTimeDelayData(clientName);
            PullTimeDelayAlarmBenchmark pullTimeDelayAlarmBenchmark
                    = monitorPullTimeDelayBenchmark(clientName);
            if (pullTimeDelayAlarmData != null && pullTimeDelayAlarmBenchmark != null) {
                map.put(pullTimeDelayAlarmData, pullTimeDelayAlarmBenchmark);
            }

            PushTimeDelayAlarmData pushTimeDelayAlarmData
                    = monitorPushTimeDelayData(clientName);
            PushTimeDelayAlarmBenchmark pushTimeDelayAlarmBenchmark
                    = monitorPushTimeDelayBenchmark(clientName);
            if (pushTimeDelayAlarmData != null && pushTimeDelayAlarmBenchmark != null) {
                map.put(pushTimeDelayAlarmData, pushTimeDelayAlarmBenchmark);
            }

            for (Map.Entry<AlarmData, AlarmBenchmark> entry : map.entrySet()) {
                AlarmData data = entry.getKey();
                AlarmBenchmark benchmark = entry.getValue();

                AlarmResult result = arbiter.arbitrate(data, benchmark);

                result = regulator.regulate(clientName, result, strategy);

                for (AlarmMeta meta : metas) {
                    notifier.notify(result, meta);
                }
            }
        }
    }

    @Override
    public PullTimeDelayAlarmData monitorPullTimeDelayData(String clientName)
            throws PumaAlarmMonitorException {
        return pumaClientAlarmDataService.findPullTimeDelay(clientName);
    }

    @Override
    public PushTimeDelayAlarmData monitorPushTimeDelayData(String clientName)
            throws PumaAlarmMonitorException {
        return pumaClientAlarmDataService.findPushTimeDelay(clientName);
    }

    @Override
    public PullTimeDelayAlarmBenchmark monitorPullTimeDelayBenchmark(String clientName)
            throws PumaAlarmMonitorException {
        return pumaClientAlarmBenchmarkService.findPullTimeDelay(clientName);
    }

    @Override
    public PushTimeDelayAlarmBenchmark monitorPushTimeDelayBenchmark(String clientName)
            throws PumaAlarmMonitorException {
        return pumaClientAlarmBenchmarkService.findPushTimeDelay(clientName);
    }

    @Override
    public AlarmStrategy monitorStrategy(String clientName) throws PumaAlarmMonitorException {
        NoAlarmStrategy noAlarmStrategy
                = pumaClientAlarmStrategyService.findNo(clientName);
        if (noAlarmStrategy != null) {
            return noAlarmStrategy;
        }

        LinearAlarmStrategy linearAlarmStrategy
                = pumaClientAlarmStrategyService.findLinear(clientName);
        if (linearAlarmStrategy != null) {
            return linearAlarmStrategy;
        }

        ExponentialAlarmStrategy exponentialAlarmStrategy
                = pumaClientAlarmStrategyService.findExponential(clientName);
        if (exponentialAlarmStrategy != null) {
            return exponentialAlarmStrategy;
        }

        return null;
    }

    @Override
    public List<AlarmMeta> monitorMetas(String clientName) throws PumaAlarmMonitorException {
        List<AlarmMeta> metas = Lists.newArrayList();

        EmailAlarmMeta emailAlarmMeta = pumaClientAlarmMetaService.findEmail(clientName);
        if (emailAlarmMeta != null) {
            metas.add(emailAlarmMeta);
        }

        WeChatAlarmMeta weChatAlarmMeta = pumaClientAlarmMetaService.findWeChat(clientName);
        if (weChatAlarmMeta != null) {
            metas.add(weChatAlarmMeta);
        }

        SmsAlarmMeta smsAlarmMeta = pumaClientAlarmMetaService.findSms(clientName);
        if (smsAlarmMeta != null) {
            metas.add(smsAlarmMeta);
        }

        LogAlarmMeta logAlarmMeta = pumaClientAlarmMetaService.findLog(clientName);
        if (logAlarmMeta != null) {
            metas.add(logAlarmMeta);
        }

        return metas;
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

    public void setPumaClientService(PumaClientService pumaClientService) {
        this.pumaClientService = pumaClientService;
    }

    public void setPumaClientAlarmDataService(PumaClientAlarmDataService pumaClientAlarmDataService) {
        this.pumaClientAlarmDataService = pumaClientAlarmDataService;
    }

    public void setPumaClientAlarmBenchmarkService(PumaClientAlarmBenchmarkService pumaClientAlarmBenchmarkService) {
        this.pumaClientAlarmBenchmarkService = pumaClientAlarmBenchmarkService;
    }

    public void setPumaClientAlarmStrategyService(PumaClientAlarmStrategyService pumaClientAlarmStrategyService) {
        this.pumaClientAlarmStrategyService = pumaClientAlarmStrategyService;
    }

    public void setPumaClientAlarmMetaService(PumaClientAlarmMetaService pumaClientAlarmMetaService) {
        this.pumaClientAlarmMetaService = pumaClientAlarmMetaService;
    }

    public void setScanIntervalInSecond(long scanIntervalInSecond) {
        this.scanIntervalInSecond = scanIntervalInSecond;
    }
}
