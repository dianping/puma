package com.dianping.puma.alarm.deploy;

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
public class PumaAlarmServerLauncher {

    private static final Logger logger = LoggerFactory.getLogger(PumaAlarmServerLauncher.class);

    private static final String PROPERTIES_FILE_PATH = "puma-alarm.properties";

    public static void main(String[] args) throws IOException {
        final ConfigManager configManager = ConfigManagerLoader.getConfigManager(PROPERTIES_FILE_PATH);

        String springXml = configManager.getConfig(PumaAlarmServerConstant.PUMA_ALARM_SPRING_XML);
        final ConfigurableApplicationContext context
                = new ClassPathXmlApplicationContext(springXml);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Shutting down puma alarm server launcher...");
                    context.close();

                } catch (Throwable t) {
                    logger.warn("Something went wrong when shutting down puma alarm server launcher.", t);
                } finally {
                    logger.info("Puma alarm server launcher is down.");
                }
            }
        }));
    }
}
