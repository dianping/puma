package com.dianping.puma.alarm.log;

import com.dianping.puma.alarm.exception.PumaAlarmLogException;
import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.service.PumaClientAlarmDataService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.common.utils.NamedThreadFactory;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public class FlushingPumaAlarmLogger extends AbstractPumaLifeCycle implements PumaAlarmLogger {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaClientAlarmDataService pumaClientAlarmDataService;

    private Clock clock = new Clock();

    private long flushIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("flushing-puma-alarm-logger-executor", false));

    private ConcurrentMap<String, Long> pullTimeMap = new MapMaker().makeMap();

    private ConcurrentMap<String, Long> pushTimeMap = new MapMaker().makeMap();

    @Override
    public void start() {
        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (Throwable t) {
                    logger.error("Failed to periodically flush puma alarm data.", t);
                }
            }
        }, 0, flushIntervalInSecond, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        super.stop();

        executor.shutdownNow();
    }

    private void flush() {
        flushPullTimeDelay();
        flushPushTimeDelay();
    }

    private void flushPullTimeDelay() {
        for (Map.Entry<String, Long> entry: pullTimeMap.entrySet()) {
            String clientName = entry.getKey();
            long pullTime = entry.getValue();
            long pullTimeDelayInSecond = clock.getTimestamp() - pullTime;
            PullTimeDelayAlarmData pullTimeDelayAlarmData = new PullTimeDelayAlarmData();
            pullTimeDelayAlarmData.setPullTimeDelayInSecond(pullTimeDelayInSecond);
            pumaClientAlarmDataService.replacePullTimeDelay(clientName, pullTimeDelayAlarmData);
        }
    }

    private void flushPushTimeDelay() {
        for (Map.Entry<String, Long> entry: pushTimeMap.entrySet()) {
            String clientName = entry.getKey();
            long pushTime = entry.getValue();
            long pushTimeDelayInSecond = clock.getTimestamp() - pushTime;
            PushTimeDelayAlarmData pushTimeDelayAlarmData = new PushTimeDelayAlarmData();
            pushTimeDelayAlarmData.setPushTimeDelayInSecond(pushTimeDelayInSecond);
            pumaClientAlarmDataService.replacePushTimeDelay(clientName, pushTimeDelayAlarmData);
        }
    }

    @Override
    public void logPullTime(String clientName, long pullTime) throws PumaAlarmLogException {
        pullTimeMap.put(clientName, pullTime);
    }

    @Override
    public void logPushTime(String clientName, long pushTime) throws PumaAlarmLogException {
        pushTimeMap.put(clientName, pushTime);
    }

    public void setPumaClientAlarmDataService(PumaClientAlarmDataService pumaClientAlarmDataService) {
        this.pumaClientAlarmDataService = pumaClientAlarmDataService;
    }

    public void setFlushIntervalInSecond(long flushIntervalInSecond) {
        this.flushIntervalInSecond = flushIntervalInSecond;
    }
}
