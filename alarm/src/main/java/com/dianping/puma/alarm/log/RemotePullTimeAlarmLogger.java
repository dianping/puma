package com.dianping.puma.alarm.log;

import com.dianping.puma.biz.service.ClientAlarmDataService;
import com.dianping.puma.common.intercept.AbstractPumaInterceptor;
import com.dianping.puma.common.intercept.exception.PumaInterceptException;
import com.dianping.puma.common.model.ClientAlarmData;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.core.dto.BinlogHttpMessage;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.common.utils.NamedThreadFactory;
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
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class RemotePullTimeAlarmLogger extends AbstractPumaInterceptor<BinlogHttpMessage> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConcurrentMap<String, Long> pullTimeMap = new MapMaker().makeMap();

    private ClientAlarmDataService clientAlarmDataService;

    private Clock clock = new Clock();

    private long flushIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("remote-pull-delay-alarm-interceptor-executor", true));

    @Override
    public void start() {
        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (Throwable t) {
                    logger.error("Failed to flush pull time periodically.", t);
                }
            }
        }, 0, flushIntervalInSecond, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        super.stop();

        executor.shutdownNow();
    }

    @Override
    public void before(BinlogHttpMessage data) throws PumaInterceptException {
        if (data instanceof BinlogGetRequest) {
            BinlogGetRequest binlogGetRequest = (BinlogGetRequest) data;

            String clientName = binlogGetRequest.getClientName();
            pullTimeMap.put(clientName, clock.getTimestamp());
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

    private void flush() {
        Iterator<Map.Entry<String, Long>> iterator = pullTimeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String clientName = entry.getKey();
            Long pullTime = entry.getValue();
            iterator.remove();

            try {
                ClientAlarmData clientAlarmData = new ClientAlarmData();
                clientAlarmData.setPullTime(pullTime);
                clientAlarmDataService.replacePullTime(clientName, clientAlarmData);
            } catch (Throwable t) {
                logger.error("Failed to flush pull time[{}] for client[{}].", pullTime, clientName, t);
            }
        }
    }

    public void setClientAlarmDataService(ClientAlarmDataService clientAlarmDataService) {
        this.clientAlarmDataService = clientAlarmDataService;
    }
}
