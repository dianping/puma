package com.dianping.puma.alarm.deploy.ha;

import com.dianping.puma.alarm.deploy.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.deploy.ha.model.AlarmServerLeader;
import com.dianping.puma.alarm.deploy.ha.service.PumaAlarmServerHeartbeatService;
import com.dianping.puma.alarm.deploy.ha.service.PumaAlarmServerLeaderService;
import com.dianping.puma.common.utils.AddressUtils;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.common.utils.NamedThreadFactory;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class ScanningAlarmServerLeaderManager implements PumaAlarmServerLeaderManager {

    private PumaAlarmServerLeaderService pumaAlarmServerLeaderService;

    private PumaAlarmServerHeartbeatService pumaAlarmServerHeartbeatService;

    private Clock clock = new Clock();

    private long scanIntervalInSecond = 5;

    private long leaderExpiredInSecond = 60;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            1, new NamedThreadFactory("scanning-alarm-server-leader-manager"));

    private LeaderChangeListener listener;

    private volatile boolean leaderTaken = false;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void addLeaderChangeListener(LeaderChangeListener listener) {
        this.listener = listener;
    }

    private void scan() {
        AlarmServerLeader leader = pumaAlarmServerLeaderService.findLeader();
        if (leader == null) {
            takeLeader();
            leaderTaken = true;
            onLeaderTaken();
        } else {
            String host = leader.getHost();
            String localhost = AddressUtils.getHostIp();

            if (host.equals(localhost)) {
                return;
            } else {
                AlarmServerHeartbeat heartbeat = pumaAlarmServerHeartbeatService.findHeartbeat(host);
                Date heartbeatTime = heartbeat.getHeartbeatTime();
                if (clock.getTimestamp() - heartbeatTime.getTime() > leaderExpiredInSecond * 1000) {
                    takeLeader();
                    leaderTaken = true;
                    onLeaderTaken();
                } else {
                    leaderTaken = false;
                    onLeaderReleased();
                }
            }
        }
    }

    private void onLeaderTaken() {
        listener.onLeaderTaken();
    }

    private void onLeaderReleased() {
        listener.onLeaderReleased();
    }

    private void takeLeader() {
        AlarmServerLeader leader = new AlarmServerLeader();
        String host = AddressUtils.getHostIp();
        leader.setHost(host);
        pumaAlarmServerLeaderService.takeLeader(leader);
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
}
