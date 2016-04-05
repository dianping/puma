package com.dianping.puma.alarm.deploy;

import com.dianping.puma.alarm.PumaAlarmServer;
import com.dianping.puma.alarm.deploy.ha.LeaderChangeListener;
import com.dianping.puma.alarm.deploy.ha.PumaAlarmServerHeartbeatManager;
import com.dianping.puma.alarm.deploy.ha.PumaAlarmServerLeaderManager;
import com.dianping.puma.common.config.ConfigManager;
import com.dianping.puma.common.config.ConfigManagerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmMonitorServerLauncher {

    private static final Logger logger = LoggerFactory.getLogger(PumaAlarmMonitorServerLauncher.class);

    private static final String PROPERTIES_FILE_PATH = "puma-alarm.properties";

    public static void main(String[] args) throws IOException {
        ConfigManager configManager = ConfigManagerLoader.getConfigManager(PROPERTIES_FILE_PATH);

        String springXml = configManager.getConfig(PumaAlarmServerConstant.PUMA_ALARM_SPRING_XML);
        final ConfigurableApplicationContext context
                = new ClassPathXmlApplicationContext(springXml);

        final PumaAlarmServerHeartbeatManager heartbeatManager
                = (PumaAlarmServerHeartbeatManager) context.getBean("pumaAlarmServerHeartbeatManager");

        final PumaAlarmServerLeaderManager leaderManager
                = (PumaAlarmServerLeaderManager) context.getBean("pumaAlarmServerLeaderManager");

        final PumaAlarmServer pumaAlarmServer = (PumaAlarmServer) context.getBean("pumaAlarmServer");

        logger.info("Starting puma alarm server heartbeat manager...");
        heartbeatManager.start();

        logger.info("Starting puma alarm server leader manager...");
        leaderManager.addLeaderChangeListener(new LeaderChangeListener() {
            @Override
            public void onLeaderTaken() {
                logger.info("Starting puma alarm server on leader taken...");
                pumaAlarmServer.start();
            }

            @Override
            public void onLeaderReleased() {
                logger.info("Stopping puma alarm server on leader released...");
                pumaAlarmServer.stop();
            }
        });
        leaderManager.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Shutting down puma alarm server launcher...");
                    heartbeatManager.stop();
                    leaderManager.stop();
                    pumaAlarmServer.stop();
                    context.stop();

                } catch (Throwable t) {
                    logger.warn("Something went wrong when shutting down puma alarm server launcher.");
                } finally {
                    logger.info("Puma alarm server launcher is down.");
                }
            }
        }));
    }
}
