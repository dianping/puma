package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.ShardDumpTask;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardDumpTaskExecutorMain {
    public static void main(String... args) {
        ShardDumpTask task = new ShardDumpTask();
//        task.setRuleName("pumadebug");
        task.setTableName("user");

        ShardDumpTaskExecutor target = new ShardDumpTaskExecutor(task);
        target.init();
        target.start();
    }
}
