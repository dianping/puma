package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.service.ShardDumpTaskService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardDumpTaskExecutorMain {
    public static void main(String... args) throws InterruptedException {
        ShardDumpTask task = new ShardDumpTask();

        task.setIndexIncrease(1000000);
        task.setDataBase("test");
        task.setTableName("user");
        task.setIndexColumnName("id");
        task.setIndexKey(0);
        task.setMaxKey(10000000);
        task.setName("debug");
        task.setShardRule("id % 100 <> 0");
        task.setTargetTableName("user_0");
        task.setTargetDataBase("test1");

        SrcDBInstance src = new SrcDBInstance();
        src.setHost("127.0.0.1");
        src.setPort(3306);
        src.setUsername("root");
        src.setPassword("root");

        DstDBInstance dst = new DstDBInstance();
        dst.setHost("127.0.0.1");
        dst.setPort(3306);
        dst.setUsername("root");
        dst.setPassword("root");

        ShardDumpTaskExecutor target = new ShardDumpTaskExecutor(task);

        ShardDumpTaskService service = mock(ShardDumpTaskService.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ShardDumpTask temp = (ShardDumpTask) invocationOnMock.getArguments()[0];
                System.out.println(temp.getBinlogInfo().getBinlogFile());
                System.out.println(temp.getBinlogInfo().getBinlogPosition());
                return null;
            }
        }).when(service).update(any(ShardDumpTask.class));

        target.setShardDumpTaskService(service);
        target.setSrcDBInstance(src);
        target.setDstDBInstance(dst);
        target.init();
        target.start();

        /*
        while (target.getTaskState().getPercent() < 100) {
            System.out.println(target.getTaskState().getStatus());
            System.out.println(target.getTaskState().getPercent());
            Thread.sleep(1000);
        }

        System.out.println(target.getTaskState().getStatus());
        System.out.println(target.getTaskState().getPercent());*/
    }
}
