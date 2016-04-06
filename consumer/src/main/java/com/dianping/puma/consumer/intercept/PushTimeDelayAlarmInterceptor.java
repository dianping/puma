package com.dianping.puma.consumer.intercept;

import com.dianping.puma.alarm.core.log.PumaAlarmLogger;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.intercept.exception.PumaInterceptException;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.common.utils.NamedThreadFactory;
import com.dianping.puma.core.dto.BinlogHttpMessage;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.model.BinlogInfo;
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
public class PushTimeDelayAlarmInterceptor extends AbstractPumaLifeCycle implements PumaMessageInterceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaAlarmLogger pumaAlarmLogger;

    private Clock clock = new Clock();

    private long flushIntervalInSecond = 5;

    private ConcurrentMap<String, Long> pushTimeMap = new MapMaker().makeMap();

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("push-time-delay-alarm-interceptor-executor", true));

    @Override
    public void start() {
        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (Throwable t) {
                    logger.error("Failed to periodically flush puma client push time delay.", t);
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
        for (Map.Entry<String, Long> entry: pushTimeMap.entrySet()) {
            String clientName = entry.getKey();
            long pushTime = entry.getValue();
            long pushTimeDelay = clock.getTimestamp() - pushTime;

            PushTimeDelayAlarmData data = new PushTimeDelayAlarmData();
            data.setPushTimeDelayInSecond(pushTimeDelay);
            pumaAlarmLogger.logPushTimeDelay(clientName, data);
        }
    }

    @Override
    public void before(BinlogHttpMessage data) throws PumaInterceptException {
        // do nothing.
    }

    @Override
    public void after(BinlogHttpMessage data) throws PumaInterceptException {
        if (data instanceof BinlogGetResponse) {
            BinlogGetResponse response = (BinlogGetResponse) data;

            String clientName = response.getClientName();
            BinlogMessage binlogMessage = response.getBinlogMessage();
            BinlogInfo binlogInfo = binlogMessage.getLastBinlogInfo();
            if (binlogInfo != null) {
                long pushTime = binlogInfo.getTimestamp();
                pushTimeMap.put(clientName, pushTime);
            }
        }
    }

    @Override
    public void clean(String clientName) {
        pushTimeMap.remove(clientName);
    }

    @Override
    public void error(BinlogHttpMessage data) throws PumaInterceptException {
        // do nothing.
    }

    public void setPumaAlarmLogger(PumaAlarmLogger pumaAlarmLogger) {
        this.pumaAlarmLogger = pumaAlarmLogger;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setFlushIntervalInSecond(long flushIntervalInSecond) {
        this.flushIntervalInSecond = flushIntervalInSecond;
    }
}
