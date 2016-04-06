package com.dianping.puma.alarm;

import com.dianping.puma.alarm.ha.LeaderChangeListener;
import com.dianping.puma.alarm.ha.PumaAlarmServerHeartbeatManager;
import com.dianping.puma.alarm.ha.PumaAlarmServerLeaderManager;
import com.dianping.puma.common.server.AbstractPumaServer;
import com.dianping.puma.common.server.PumaServer;

/**
 * Created by xiaotian.li on 16/4/6.
 * Email: lixiaotian07@gmail.com
 */
public class HotHAAlarmServer extends AbstractPumaServer implements PumaServer {

    private PumaAlarmServerHeartbeatManager pumaAlarmServerHeartbeatManager;

    private PumaAlarmServerLeaderManager pumaAlarmServerLeaderManager;

    private StandaloneAlarmServer standaloneAlarmServer;

    @Override
    public void start() {
        super.start();

        pumaAlarmServerHeartbeatManager.start();

        pumaAlarmServerLeaderManager.addLeaderChangeListener(new LeaderChangeListener() {
            @Override
            public void onLeaderTaken() {
                standaloneAlarmServer.start();
            }

            @Override
            public void onLeaderReleased() {
                standaloneAlarmServer.stop();
            }
        });
        pumaAlarmServerLeaderManager.start();
    }

    @Override
    public void stop() {
        super.stop();

        pumaAlarmServerHeartbeatManager.stop();
        pumaAlarmServerLeaderManager.stop();
        if (standaloneAlarmServer.isStart()) {
            standaloneAlarmServer.stop();
        }
    }

    public void setPumaAlarmServerHeartbeatManager(PumaAlarmServerHeartbeatManager pumaAlarmServerHeartbeatManager) {
        this.pumaAlarmServerHeartbeatManager = pumaAlarmServerHeartbeatManager;
    }

    public void setPumaAlarmServerLeaderManager(PumaAlarmServerLeaderManager pumaAlarmServerLeaderManager) {
        this.pumaAlarmServerLeaderManager = pumaAlarmServerLeaderManager;
    }

    public void setStandaloneAlarmServer(StandaloneAlarmServer standaloneAlarmServer) {
        this.standaloneAlarmServer = standaloneAlarmServer;
    }
}
