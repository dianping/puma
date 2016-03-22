package com.dianping.puma.alarm.log;

import com.dianping.puma.alarm.exception.PumaAlarmLogException;
import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.service.PumaClientAlarmDataService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
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

    private long flushIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("flushing-puma-alarm-logger-executor", false));

    private ConcurrentMap<String, PullTimeDelayAlarmData> pullTimeDelayAlarmDataMap
            = new MapMaker().makeMap();

    private ConcurrentMap<String, PushTimeDelayAlarmData> pushTimeDelayAlarmDataMap
            = new MapMaker().makeMap();

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
        for (Map.Entry<String, PullTimeDelayAlarmData> entry: pullTimeDelayAlarmDataMap.entrySet()) {
            String clientName = entry.getKey();
            PullTimeDelayAlarmData data = entry.getValue();
            pumaClientAlarmDataService.replacePullTimeDelay(clientName, data);
        }
    }

    private void flushPushTimeDelay() {
        for (Map.Entry<String, PushTimeDelayAlarmData> entry: pushTimeDelayAlarmDataMap.entrySet()) {
            String clientName = entry.getKey();
            PushTimeDelayAlarmData data = entry.getValue();
            pumaClientAlarmDataService.replacePushTimeDelay(clientName, data);
        }
    }

    @Override
    public void logPullTimeDelay(String clientName, PullTimeDelayAlarmData data) throws PumaAlarmLogException {
        pullTimeDelayAlarmDataMap.put(clientName, data);
    }

    @Override
    public void logPushTimeDelay(String clientName, PushTimeDelayAlarmData data) throws PumaAlarmLogException {
        pushTimeDelayAlarmDataMap.put(clientName, data);
    }

    public void setPumaClientAlarmDataService(PumaClientAlarmDataService pumaClientAlarmDataService) {
        this.pumaClientAlarmDataService = pumaClientAlarmDataService;
    }

    public void setFlushIntervalInSecond(long flushIntervalInSecond) {
        this.flushIntervalInSecond = flushIntervalInSecond;
    }
}
