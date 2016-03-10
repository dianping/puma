package com.dianping.puma.alarm.log.remote;

import com.dianping.puma.biz.service.ClientAlarmDataService;
import com.dianping.puma.common.intercept.AbstractPumaInterceptor;
import com.dianping.puma.common.intercept.exception.PumaInterceptException;
import com.dianping.puma.core.dto.BinlogHttpMessage;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.NamedThreadFactory;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public class RemotePushTimeAlarmLogger extends AbstractPumaInterceptor<BinlogHttpMessage> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConcurrentMap<String, Long> pushTimeMap = new MapMaker().makeMap();

    private ClientAlarmDataService clientAlarmDataService;

    private long flushIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("remote-push-time-alarm-logger", true));

    @Override
    public void start() {
        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (Throwable t) {
                    logger.error("Failed to periodically flush push time.", t);
                }
            }
        }, 0, flushIntervalInSecond, TimeUnit.SECONDS);
    }

    private void flush() {
        Iterator<Map.Entry<String, Long>> iterator = pushTimeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String clientName = entry.getKey();
            Long pushTime = entry.getValue();
            iterator.remove();

            try {
                clientAlarmDataService.updatePushTimestamp(clientName, pushTime);
            } catch (Throwable t) {
                logger.error("Failed to flush push time[{}] for client[{}].",
                        pushTime, clientName, t);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();

        executor.shutdownNow();
    }

    @Override
    public void before(BinlogHttpMessage data) throws PumaInterceptException {
        // do nothing.
    }

    @Override
    public void after(BinlogHttpMessage data) throws PumaInterceptException {
        if (data instanceof BinlogGetResponse) {
            BinlogGetResponse binlogGetResponse = (BinlogGetResponse) data;

            String clientName = binlogGetResponse.getClientName();

            BinlogMessage binlogMessage = binlogGetResponse.getBinlogMessage();
            BinlogInfo binlogInfo = binlogMessage.getLastBinlogInfo();
            if (binlogInfo != null) {
                Long pushTime = binlogInfo.getTimestamp();
                pushTimeMap.put(clientName, pushTime);
            }
        }
    }

    @Override
    public void error(BinlogHttpMessage data) throws PumaInterceptException {
        // do nothing.
    }

    public void setClientAlarmDataService(ClientAlarmDataService clientAlarmDataService) {
        this.clientAlarmDataService = clientAlarmDataService;
    }

    public void setFlushIntervalInSecond(long flushIntervalInSecond) {
        this.flushIntervalInSecond = flushIntervalInSecond;
    }
}
