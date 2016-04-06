package com.dianping.puma.alarm.ha;

import com.dianping.puma.alarm.exception.PumaAlarmServerLeaderManageException;
import com.dianping.puma.alarm.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.ha.model.AlarmServerLeader;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerHeartbeatService;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerLeaderService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.utils.AddressUtils;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.common.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class ScanningAlarmServerLeaderManager extends AbstractPumaLifeCycle implements PumaAlarmServerLeaderManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaAlarmServerLeaderService pumaAlarmServerLeaderService;

    private PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService;

    private Clock clock;

    private long scanIntervalInSecond = 5;

    private long leaderExpiredInSecond = 60;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("scanning-alarm-server-leader-manager", false));

    private LeaderChangeListener listener;

    private volatile boolean leader = false;

    @Override
    public void start() {
        logger.info("Starting puma scanning alarm server leader manager...");

        super.start();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Scanning puma alarm server leader at the rate of {}s...", scanIntervalInSecond);
                    }
                    scan();
                } catch (Throwable t) {
                    logger.error("Failed to periodically scan puma alarm server leader.", t);
                }
            }
        }, 0, scanIntervalInSecond, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        logger.info("Stopping puma scanning alarm server leader manager...");

        super.stop();

        // Release the leader first.
        if (leader) {
            releaseLeader();
            onLeaderReleased();
            leader = false;
        }
        executor.shutdownNow();
    }

    @Override
    public String findLeader() throws PumaAlarmServerLeaderManageException {
        AlarmServerLeader leader = pumaAlarmServerLeaderService.findLeader();
        if (leader == null) {
            return null;
        } else {
            String host = leader.getHost();
            AlarmServerHeartbeat heartbeat = pumaAlarmServerHeartbeatService.findHeartbeat(host);
            long heartbeatTime = heartbeat.getHeartbeatTime().getTime() / 1000;
            long now = clock.getTimestamp();
            return now - heartbeatTime <= leaderExpiredInSecond ? host : null;
        }
    }

    @Override
    public boolean tryTakeLeader() throws PumaAlarmServerLeaderManageException {
        AlarmServerLeader leader = new AlarmServerLeader();

        String localhost = AddressUtils.getHostIp();
        leader.setHost(localhost);

        return pumaAlarmServerLeaderService.takeLeader(leader);
    }

    @Override
    public void releaseLeader() throws PumaAlarmServerLeaderManageException {
        String localhost = AddressUtils.getHostIp();
        pumaAlarmServerLeaderService.releaseLeader(localhost);
    }

    @Override
    public void addLeaderChangeListener(LeaderChangeListener listener) {
        this.listener = listener;
    }

    private void scan() {
        if (leader) {
            String leaderHost = findLeader();
            String localhost = AddressUtils.getHostIp();
            if (!leaderHost.equals(localhost)) {
                onLeaderReleased();
                leader = false;
            }
        } else {
            leader = tryTakeLeader();
            if (leader) {
                onLeaderTaken();
                leader = true;
            }
        }
    }

    private void onLeaderTaken() {
        listener.onLeaderTaken();
    }

    private void onLeaderReleased() {
        listener.onLeaderReleased();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setPumaAlarmServerLeaderService(PumaAlarmServerLeaderService pumaAlarmServerLeaderService) {
        this.pumaAlarmServerLeaderService = pumaAlarmServerLeaderService;
    }

    public void setPumaAlarmServerHeartbeatService(PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService) {
        this.pumaAlarmServerHeartbeatService = pumaAlarmServerHeartbeatService;
    }

    public void setScanIntervalInSecond(long scanIntervalInSecond) {
        this.scanIntervalInSecond = scanIntervalInSecond;
    }

    public void setLeaderExpiredInSecond(long leaderExpiredInSecond) {
        this.leaderExpiredInSecond = leaderExpiredInSecond;
    }
}
