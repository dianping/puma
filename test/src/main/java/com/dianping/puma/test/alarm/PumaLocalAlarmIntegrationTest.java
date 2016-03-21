package com.dianping.puma.test.alarm;

import com.dianping.puma.alarm.monitor.PumaAlarmMonitor;
import com.dianping.puma.biz.service.memory.MemoryClientAlarmBenchmarkService;
import com.dianping.puma.biz.service.memory.MemoryClientAlarmDataService;
import com.dianping.puma.biz.service.memory.MemoryClientAlarmMetaService;
import com.dianping.puma.biz.service.memory.MemoryClientAlarmStrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class PumaLocalAlarmIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(PumaLocalAlarmIntegrationTest.class);

    public static void main(String[] args) throws IOException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring/alarm/local.xml");
        final PumaAlarmMonitor monitor = (PumaAlarmMonitor) ctx.getBean("pumaAlarmMonitor");

        MemoryClientAlarmDataService dataService
                = (MemoryClientAlarmDataService) ctx.getBean("memoryClientAlarmDataService");

        MemoryClientAlarmBenchmarkService benchmarkService
                = (MemoryClientAlarmBenchmarkService) ctx.getBean("memoryClientAlarmBenchmarkService");

        MemoryClientAlarmStrategyService strategyService
                = (MemoryClientAlarmStrategyService) ctx.getBean("memoryClientAlarmStrategyService");

        MemoryClientAlarmMetaService metaService
                = (MemoryClientAlarmMetaService) ctx.getBean("memoryClientAlarmMetaService");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        thread.setDaemon(false);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Shutdown puma alarm monitor.");
                    monitor.stop();
                } catch (Throwable t) {
                    logger.warn("Something went wrong when shutting down puma alarm monitor.");
                } finally {
                    logger.info("Puma alarm monitor is down.");
                }
            }
        }));
    }
}
