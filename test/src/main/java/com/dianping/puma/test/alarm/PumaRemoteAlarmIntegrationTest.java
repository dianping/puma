package com.dianping.puma.test.alarm;

import com.dianping.puma.alarm.core.monitor.PumaAlarmMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class PumaRemoteAlarmIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(PumaLocalAlarmIntegrationTest.class);

    public static void main(String[] args) throws IOException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:spring/alarm/appcontext.xml",
                "classpath:spring/alarm/appcontext-service-remote.xml",
                "classpath:spring/db/appcontext-dao.xml",
                "classpath:spring/db/appcontext-db.xml");
        final PumaAlarmMonitor pumaAlarmMonitor = (PumaAlarmMonitor) ctx.getBean("pumaAlarmMonitor");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Shutdown puma alarm monitor.");
                    pumaAlarmMonitor.stop();
                } catch (Throwable t) {
                    logger.warn("Something went wrong when shutting down puma alarm monitor.");
                } finally {
                    logger.info("Puma alarm monitor is down.");
                }
            }
        }));
    }
}
