package com.dianping.puma.test.alarm;

import com.dianping.puma.alarm.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.meta.LogAlarmMeta;
import com.dianping.puma.alarm.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.monitor.PumaAlarmMonitor;
import com.dianping.puma.biz.service.memory.*;
import com.dianping.puma.common.model.Client;
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

        final MemoryClientService clientService
                = (MemoryClientService) ctx.getBean("memoryClientService");

        final MemoryClientAlarmDataService dataService
                = (MemoryClientAlarmDataService) ctx.getBean("memoryClientAlarmDataService");

        final MemoryClientAlarmBenchmarkService benchmarkService
                = (MemoryClientAlarmBenchmarkService) ctx.getBean("memoryClientAlarmBenchmarkService");

        final MemoryClientAlarmStrategyService strategyService
                = (MemoryClientAlarmStrategyService) ctx.getBean("memoryClientAlarmStrategyService");

        final MemoryClientAlarmMetaService metaService
                = (MemoryClientAlarmMetaService) ctx.getBean("memoryClientAlarmMetaService");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Client client = new Client();
                client.setClientName("test0");
                clientService.create(client);

                PullTimeDelayAlarmData pullTimeDelayAlarmData = new PullTimeDelayAlarmData();
                pullTimeDelayAlarmData.setPullTimeDelayInSecond(10);
                dataService.replacePullTimeDelay("test0", pullTimeDelayAlarmData);

                PullTimeDelayAlarmBenchmark pullTimeDelayAlarmBenchmark = new PullTimeDelayAlarmBenchmark();
                pullTimeDelayAlarmBenchmark.setPullTimeDelayAlarm(true);
                pullTimeDelayAlarmBenchmark.setMinPullTimeDelayInSecond(5);
                pullTimeDelayAlarmBenchmark.setMaxPullTimeDelayInSecond(9);
                benchmarkService.replacePullTimeDelay("test0", pullTimeDelayAlarmBenchmark);

                LinearAlarmStrategy linearAlarmStrategy = new LinearAlarmStrategy();
                linearAlarmStrategy.setLinearAlarmIntervalInSecond(10);
                strategyService.replaceLinear("test0", linearAlarmStrategy);

                LogAlarmMeta logAlarmMeta = new LogAlarmMeta();
                metaService.replaceLog("test0", logAlarmMeta);
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
