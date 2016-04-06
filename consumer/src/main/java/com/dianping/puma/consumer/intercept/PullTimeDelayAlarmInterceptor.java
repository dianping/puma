package com.dianping.puma.consumer.intercept;

import com.dianping.puma.alarm.core.log.PumaAlarmLogger;
import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.intercept.exception.PumaInterceptException;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.common.utils.NamedThreadFactory;
import com.dianping.puma.core.dto.BinlogHttpMessage;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
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
public class PullTimeDelayAlarmInterceptor extends AbstractPumaLifeCycle implements PumaMessageInterceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaAlarmLogger pumaAlarmLogger;

    private Clock clock = new Clock();

    private long flushIntervalInSecond = 5;

    private ConcurrentMap<String, Long> pullTimeMap = new MapMaker().makeMap();

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("pull-time-delay-alarm-interceptor-executor", true));

    @Override
    public void start() {
        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (Throwable t) {
                    logger.error("Failed to periodically flush puma client pull time delay.", t);
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
        for (Map.Entry<String, Long> entry: pullTimeMap.entrySet()) {
            String clientName = entry.getKey();
            long pullTime = entry.getValue();
            long pullTimeDelay = clock.getTimestamp() - pullTime;

            PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
            data.setPullTimeDelayInSecond(pullTimeDelay);
            pumaAlarmLogger.logPullTimeDelay(clientName, data);
        }
    }

    @Override
    public void before(BinlogHttpMessage data) throws PumaInterceptException {
        if (data instanceof BinlogGetRequest) {
            BinlogGetRequest binlogGetRequest = (BinlogGetRequest) data;

            String clientName = binlogGetRequest.getClientName();
            long now = clock.getTimestamp();
            pullTimeMap.put(clientName, now);
        }
    }

    @Override
    public void after(BinlogHttpMessage data) throws PumaInterceptException {
        // do nothing.
    }

    @Override
    public void error(BinlogHttpMessage data) throws PumaInterceptException {
        // do nothing.
    }

    @Override
    public void clean(String clientName) {
        pullTimeMap.remove(clientName);
    }

    public void setPumaAlarmLogger(PumaAlarmLogger pumaAlarmLogger) {
        this.pumaAlarmLogger = pumaAlarmLogger;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
