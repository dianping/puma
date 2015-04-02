package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.SrcDBInstance;

import java.io.IOException;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardDumpTaskExecutorMain {
    public static void main(String... args) throws IOException {
        ShardDumpTask task = new ShardDumpTask();
        task.setDataBase("test");
        task.setTableName("user");
        task.setIndexColumnName("id");
        task.setIndexKey(0);
        task.setMaxKey(10000000);
        task.setName("debug");
        task.setShardRule("id % 3 = 1");
        task.setTargetTableName("user_1");

        SrcDBInstance src = new SrcDBInstance();
        src.setHost("127.0.0.1");
        src.setPort(3306);
        src.setUsername("root");
        src.setPassword("root");

        ShardDumpTaskExecutor target = new ShardDumpTaskExecutor(task);
        target.setSrcDBInstance(src);
        target.init();
        target.start();

        System.in.read();
    }
}
