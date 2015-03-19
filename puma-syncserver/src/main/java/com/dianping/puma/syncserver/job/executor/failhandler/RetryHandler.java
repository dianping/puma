package com.dianping.puma.syncserver.job.executor.failhandler;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.dianping.puma.core.entity.AbstractBaseSyncTask;
import com.dianping.puma.core.entity.BaseSyncTask;
import com.dianping.puma.core.model.SyncTaskState;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.job.executor.AbstractTaskExecutor;

public class RetryHandler implements Handler {

    private static final String PATTERN         = "yyyyMMdd HH:mm:ss";

    private static final Logger LOG             = LoggerFactory.getLogger(StopOnFailedHandler.class);

    private long                sleepTime       = 5;

    private ExecutorService     executorService = Executors.newSingleThreadExecutor();

    @Override
    public String getName() {
        return "Retry";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public HandleResult handle(HandleContext context) {
        HandleResult result = new HandleResult();

        final AbstractTaskExecutor executor = context.getExecutor();
        BaseSyncTask task = context.getTask();
        Exception exception = context.getException();
        ChangedEvent changedEvent = context.getChangedEvent();

        SyncTaskState state = executor.getState();
        //TaskExecutorStatus status = executor.getStatus();
        state.setDetail("RetryHandler retrying, cause: " + task.getPumaTaskName() + "->" + task.getDstDBInstanceName() + ":" + exception.getMessage() + ". Event=" + changedEvent + ", at "
                + DateFormatUtils.format(new Date(), PATTERN));

        //思路：errorcode不是-1时，认为是正常的sql异常，则重试; 否则认为是网络之类的不可恢复问题，则重启task

        //1. 属于mysql异常，则重试
        if (exception instanceof SQLException) {
            SQLException se = (SQLException) exception;
            int errorCode = se.getErrorCode();
            if (errorCode > 0) {
                result.setIgnoreFailEvent(false);
                sleep();
                return result;
            }
        }

        //2. 否则，重启task(由于当前handler是在task中pumaClient的线程中运行，要重启的话，需要另外一个线程去restart)
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                executor.restart();
            }
        });
        sleep();

        result.setIgnoreFailEvent(false);//在restart里，老的pumaclient已经被stop，result已经故无所谓
        return result;
    }

    private void sleep() {
        try {
            LOG.info("Sleep " + sleepTime + "s");
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

}
