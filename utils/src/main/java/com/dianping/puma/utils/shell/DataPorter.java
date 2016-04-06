package com.dianping.puma.utils.shell;

import com.dianping.puma.alarm.core.service.PumaClientAlarmBenchmarkService;
import com.dianping.puma.alarm.core.service.PumaClientAlarmDataService;
import com.dianping.puma.alarm.core.service.PumaClientAlarmMetaService;
import com.dianping.puma.alarm.core.service.PumaClientAlarmStrategyService;
import com.dianping.puma.biz.dao.ClientAlarmBenchmarkDao;
import com.dianping.puma.biz.dao.ClientAlarmDataDao;
import com.dianping.puma.biz.dao.ClientAlarmMetaDao;
import com.dianping.puma.biz.dao.ClientAlarmStrategyDao;
import com.dianping.puma.biz.entity.ClientAlarmBenchmarkEntity;
import com.dianping.puma.biz.entity.ClientAlarmDataEntity;
import com.dianping.puma.biz.entity.ClientAlarmMetaEntity;
import com.dianping.puma.biz.entity.ClientAlarmStrategyEntity;
import com.dianping.puma.common.service.PumaClientService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/24.
 * Email: lixiaotian07@gmail.com
 */
public class DataPorter {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:spring/utils-dao.xml",
                "classpath:spring/utils-db.xml",
                "classpath:spring/utils-remote.xml",
                "classpath:spring/utils-tx.xml"
        );

        PumaClientService pumaClientService
                = (PumaClientService) ctx.getBean("pumaClientService");
        PumaClientAlarmDataService pumaClientAlarmDataService
                = (PumaClientAlarmDataService) ctx.getBean("pumaClientAlarmDataService");
        PumaClientAlarmBenchmarkService pumaClientAlarmBenchmarkService
                = (PumaClientAlarmBenchmarkService) ctx.getBean("pumaClientAlarmBenchmarkService");
        PumaClientAlarmStrategyService pumaClientAlarmStrategyService
                = (PumaClientAlarmStrategyService) ctx.getBean("pumaClientAlarmStrategyService");
        PumaClientAlarmMetaService pumaClientAlarmMetaService
                = (PumaClientAlarmMetaService) ctx.getBean("pumaClientAlarmMetaService");

        ClientAlarmDataDao clientAlarmDataDao = (ClientAlarmDataDao) ctx.getBean("clientAlarmDataDao");
        ClientAlarmBenchmarkDao clientAlarmBenchmarkDao = (ClientAlarmBenchmarkDao) ctx.getBean("clientAlarmBenchmarkDao");
        ClientAlarmStrategyDao clientAlarmStrategyDao = (ClientAlarmStrategyDao) ctx.getBean("clientAlarmStrategyDao");
        ClientAlarmMetaDao clientAlarmMetaDao = (ClientAlarmMetaDao) ctx.getBean("clientAlarmMetaDao");

        List<String> clientNames = pumaClientService.findAllClientNames();
        for (String clientName : clientNames) {
            try {
                ClientAlarmDataEntity entity = new ClientAlarmDataEntity();
                entity.setClientName(clientName);
                clientAlarmDataDao.insert(entity);
            } catch (Throwable ignore) {

            }

            try {
                ClientAlarmBenchmarkEntity entity = new ClientAlarmBenchmarkEntity();
                entity.setClientName(clientName);
                clientAlarmBenchmarkDao.insert(entity);
            } catch (Throwable ignore) {

            }

            try {
                ClientAlarmStrategyEntity entity = new ClientAlarmStrategyEntity();
                entity.setClientName(clientName);
                clientAlarmStrategyDao.insert(entity);
            } catch (Throwable ignore) {

            }

            try {
                ClientAlarmMetaEntity entity = new ClientAlarmMetaEntity();
                entity.setClientName(clientName);
                clientAlarmMetaDao.insert(entity);
            } catch (Throwable ignore) {

            }
        }


        System.out.println(clientNames);
    }
}
