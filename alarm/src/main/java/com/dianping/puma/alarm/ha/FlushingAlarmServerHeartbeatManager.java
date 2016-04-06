package com.dianping.puma.alarm.ha;

import com.dianping.puma.alarm.exception.PumaAlarmServerHeartbeatManageException;
import com.dianping.puma.alarm.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerHeartbeatService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.utils.AddressUtils;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.common.utils.NamedThreadFactory;
import com.dianping.puma.common.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class FlushingAlarmServerHeartbeatManager extends AbstractPumaLifeCycle implements PumaAlarmServerHeartbeatManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService;

    private Clock clock = new Clock();

    private long flushIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("flushing-alarm-server-heartbeat-manager", false));

    @Override
    public void start() {
        logger.info("Starting puma flushing alarm server heartbeat manager...");

        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Flushing alarm server heartbeat at the rate of {}s...", flushIntervalInSecond);
                    }

                    flush();
                } catch (Throwable t) {
                    logger.error("Failed to periodically flush alarm server heartbeat.", t);
                }
            }
        }, 0, flushIntervalInSecond, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        logger.info("Stopping puma flushing alarm server heartbeat manager...");

        super.stop();

        executor.shutdownNow();
    }

    private void flush() {
        heartbeat();
    }

    @Override
    public void heartbeat() throws PumaAlarmServerHeartbeatManageException {
        AlarmServerHeartbeat heartbeat = new AlarmServerHeartbeat();

        String localhost = AddressUtils.getHostIp();
        heartbeat.setHost(localhost);

        Date now = clock.getTime();
        heartbeat.setHeartbeatTime(now);

        double load = SystemUtils.getNormalizedLoadAverage();
        heartbeat.setLoadAverage(load);

        pumaAlarmServerHeartbeatService.heartbeat(heartbeat);
    }

    public void setPumaAlarmServerHeartbeatService(PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService) {
        this.pumaAlarmServerHeartbeatService = pumaAlarmServerHeartbeatService;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setFlushIntervalInSecond(long flushIntervalInSecond) {
        this.flushIntervalInSecond = flushIntervalInSecond;
    }
}
