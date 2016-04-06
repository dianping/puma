package com.dianping.puma.alarm;

import com.dianping.puma.alarm.ha.LeaderChangeListener;
import com.dianping.puma.alarm.ha.PumaAlarmServerHeartbeatManager;
import com.dianping.puma.alarm.ha.PumaAlarmServerLeaderManager;
import com.dianping.puma.common.server.AbstractPumaServer;
import com.dianping.puma.common.server.PumaServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiaotian.li on 16/4/6.
 * Email: lixiaotian07@gmail.com
 */
public class HotHAAlarmServer extends AbstractPumaServer implements PumaServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaAlarmServerHeartbeatManager pumaAlarmServerHeartbeatManager;

    private PumaAlarmServerLeaderManager pumaAlarmServerLeaderManager;

    private StandaloneAlarmServer standaloneAlarmServer;

    @Override
    public void start() {
        logger.info("Starting puma hot ha alarm server...");

        super.start();

        pumaAlarmServerHeartbeatManager.start();

        pumaAlarmServerLeaderManager.addLeaderChangeListener(new LeaderChangeListener() {
            @Override
            public void onLeaderTaken() {
                logger.info("Starting puma standalone alarm server when leader taken...");

                standaloneAlarmServer.start();
            }

            @Override
            public void onLeaderReleased() {
                logger.info("Stopping puma standalone alarm server when leader released...");

                standaloneAlarmServer.stop();
            }
        });
        pumaAlarmServerLeaderManager.start();
    }

    @Override
    public void stop() {
        logger.info("Stopping puma hot ha alarm server...");

        super.stop();

        pumaAlarmServerHeartbeatManager.stop();
        pumaAlarmServerLeaderManager.stop();
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
