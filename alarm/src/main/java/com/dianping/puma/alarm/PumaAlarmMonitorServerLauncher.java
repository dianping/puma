package com.dianping.puma.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmMonitorServerLauncher {

    private static Logger logger = LoggerFactory.getLogger(PumaAlarmMonitorServerLauncher.class);

    public static void main(String[] args) {
        final ConfigurableApplicationContext ctx
                = new ClassPathXmlApplicationContext("classpath:spring/monitor-local.xml");

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
