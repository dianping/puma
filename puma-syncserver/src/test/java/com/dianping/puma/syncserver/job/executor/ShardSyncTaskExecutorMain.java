package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.client.LionException;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.entity.ShardSyncTask;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardSyncTaskExecutorMain {

    private static ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

    public static void main(String... args) throws LionException {
        ShardSyncTask shardSyncTask = new ShardSyncTask();
        shardSyncTask.setTableName("user");
        shardSyncTask.setRuleName("pumadebug");

        ShardSyncTaskExecutor shardSyncTaskExecutor = new ShardSyncTaskExecutor(shardSyncTask);
        shardSyncTaskExecutor.setSrcDBInstanceService(ctx.getBean(SrcDBInstanceService.class));
        shardSyncTaskExecutor.setPumaTaskService(ctx.getBean(PumaTaskService.class));
        shardSyncTaskExecutor.setPumaServerService(ctx.getBean(PumaServerService.class));

        shardSyncTaskExecutor.init();

        shardSyncTaskExecutor.start();
    }
}
