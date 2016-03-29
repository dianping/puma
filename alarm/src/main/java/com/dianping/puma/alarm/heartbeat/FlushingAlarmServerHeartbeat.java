package com.dianping.puma.alarm.heartbeat;

import com.dianping.puma.alarm.exception.PumaAlarmServerHeartbeatException;
import com.dianping.puma.alarm.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.service.PumaAlarmServerHeartbeatService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.utils.AddressUtils;
import com.dianping.puma.common.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/3/29.
 * Email: lixiaotian07@gmail.com
 */
public class FlushingAlarmServerHeartbeat extends AbstractPumaLifeCycle implements PumaAlarmServerHeartbeat {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService;

    private long flushingIntervalInSecond = 5;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("flushing-alarm-server-heartbeat-executor"));

    @Override
    public void start() {
        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    flush();
                } catch (Throwable t) {
                    logger.error("Failed to periodically flush alarm server heartbeat.", t);
                }
            }
        }, 0, flushingIntervalInSecond, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        super.stop();

        executor.shutdownNow();
    }

    private void flush() {
        AlarmServerHeartbeat heartbeat = new AlarmServerHeartbeat();
        heartbeat.setHeartbeatTime(new Date());
        heartbeat(heartbeat);
    }

    @Override
    public void heartbeat(AlarmServerHeartbeat heartbeat) throws PumaAlarmServerHeartbeatException {
        String host = AddressUtils.getHostIp();

        int result = pumaAlarmServerHeartbeatService.update(host, heartbeat);
        if (result == 0) {
            try {
                pumaAlarmServerHeartbeatService.create(host, heartbeat);
            } catch (Throwable ignore) {
                pumaAlarmServerHeartbeatService.update(host, heartbeat);
            }
        }
    }

    public void setPumaAlarmServerHeartbeatService(PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService) {
        this.pumaAlarmServerHeartbeatService = pumaAlarmServerHeartbeatService;
    }
}
