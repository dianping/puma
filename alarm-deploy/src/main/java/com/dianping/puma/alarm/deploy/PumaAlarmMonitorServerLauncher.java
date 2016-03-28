package com.dianping.puma.alarm.deploy;

import com.dianping.puma.common.config.ConfigManager;
import com.dianping.puma.common.config.ConfigManagerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmMonitorServerLauncher {

    private static final Logger logger = LoggerFactory.getLogger(PumaAlarmMonitorServerLauncher.class);

    private static final String PROPERTIES_FILE_PATH = "puma-alarm.properties";

    public static void main(String[] args) throws IOException {
        String springXml;

        ConfigManager configManager = ConfigManagerLoader.getConfigManager();
        if (configManager == null) {
            Properties properties = new Properties();
            InputStream inputStream
                    = PumaAlarmMonitorServerLauncher.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_PATH);
            properties.load(inputStream);

            springXml = properties.getProperty(PumaAlarmMonitorServerConstant.PUMA_ALARM_MONITOR_SPRING_XML);
            if (springXml == null) {
                throw new NullPointerException("springXml");
            }
        } else {
            springXml = configManager.getConfig(PumaAlarmMonitorServerConstant.PUMA_ALARM_MONITOR_SPRING_XML);
        }

        final ConfigurableApplicationContext ctx
                = new ClassPathXmlApplicationContext(springXml);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Shutting down puma alarm monitor server...");
                    ctx.close();
                } catch (Throwable t) {
                    logger.warn("Something went wrong when shutting down puma alarm monitor server.");
                } finally {
                    logger.info("Puma alarm monitor server is down.");
                }
            }
        }));
    }
}
