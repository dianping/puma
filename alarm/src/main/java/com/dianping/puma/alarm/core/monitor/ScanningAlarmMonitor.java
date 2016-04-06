package com.dianping.puma.alarm.core.monitor;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmMessage;
import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.meta.*;
import com.dianping.puma.alarm.core.model.state.AlarmState;
import com.dianping.puma.alarm.core.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.NoAlarmStrategy;
import com.dianping.puma.alarm.core.monitor.filter.PumaAlarmFilter;
import com.dianping.puma.alarm.core.monitor.judge.PumaAlarmJudger;
import com.dianping.puma.alarm.core.monitor.notify.PumaAlarmNotifier;
import com.dianping.puma.alarm.core.monitor.render.PumaAlarmRenderer;
import com.dianping.puma.alarm.core.service.PumaClientAlarmBenchmarkService;
import com.dianping.puma.alarm.core.service.PumaClientAlarmDataService;
import com.dianping.puma.alarm.core.service.PumaClientAlarmMetaService;
import com.dianping.puma.alarm.core.service.PumaClientAlarmStrategyService;
import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
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

    private PumaAlarmJudger arbiter;

    private PumaAlarmRenderer renderer;

    private PumaAlarmFilter filter;

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
        logger.info("Starting puma scanning alarm monitor...");

        super.start();

        arbiter.start();
        filter.start();
        notifier.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Scanning puma alarms at the rate of {}s...", scanIntervalInSecond);
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
        logger.info("Stopping puma scanning alarm monitor...");

        super.stop();

        executor.shutdownNow();

        notifier.stop();
        filter.stop();
        arbiter.stop();
    }

    private void scan() {
        List<String> clientNames = pumaClientService.findAllClientNames();

        for (String clientName : clientNames) {
            AlarmStrategy strategy = monitorStrategy(clientName);
            List<AlarmMeta> metas = monitorMetas(clientName);

            if (strategy == null) {
                continue;
            }

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

            AlarmContext context = new AlarmContext();
            context.setNamespace("client");
            context.setName(clientName);

            for (Map.Entry<AlarmData, AlarmBenchmark> entry : map.entrySet()) {
                AlarmData data = entry.getKey();
                AlarmBenchmark benchmark = entry.getValue();

                AlarmState state = arbiter.judge(data, benchmark);
                AlarmMessage message = renderer.render(context, data, benchmark);
                AlarmResult result = filter.filter(context, state, strategy);
                result.setTitle(message.getTitle());
                result.setContent(message.getContent());

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

    public void setArbiter(PumaAlarmJudger arbiter) {
        this.arbiter = arbiter;
    }

    public void setRenderer(PumaAlarmRenderer renderer) {
        this.renderer = renderer;
    }

    public void setFilter(PumaAlarmFilter filter) {
        this.filter = filter;
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
