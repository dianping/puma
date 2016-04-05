package com.dianping.puma.alarm.deploy.ha;

import com.dianping.puma.alarm.deploy.exception.PumaAlarmServerHeartbeatManageException;
import com.dianping.puma.alarm.deploy.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.deploy.ha.service.PumaAlarmServerHeartbeatService;
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
public class FlushingAlarmServerHeartbeatManager implements PumaAlarmServerHeartbeatManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService;

    private Clock clock = new Clock();

    private long flushIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("flushing-alarm-server-heartbeat-manager"));

    @Override
    public void start() {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (Throwable t) {
                    logger.error("Failed to periodically flush alarm server heartbeat.", t);
                }
            }
        }, 0, flushIntervalInSecond, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
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
