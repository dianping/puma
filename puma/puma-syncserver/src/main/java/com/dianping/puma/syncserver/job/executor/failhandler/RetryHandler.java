package com.dianping.puma.syncserver.job.executor.failhandler;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.job.executor.AbstractTaskExecutor;

public class RetryHandler implements Handler {

    private static final Logger LOG       = LoggerFactory.getLogger(StopOnFailedHandler.class);

    private long                sleepTime = 5;

    @Override
    public String getName() {
        return "Retry";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public HandleResult handle(HandleContext context) {
        AbstractTaskExecutor executor = context.getExecutor();
        Task task = context.getTask();
        Exception exception = context.getException();
        ChangedEvent changedEvent = context.getChangedEvent();

        try {
            LOG.info("Sleep " + sleepTime + "s");
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
        }

        TaskExecutorStatus status = executor.getStatus();
        status.setDetail("RetryHandler retrying, cause: " + task.getSrcMysqlName() + "->" + task.getDestMysqlName() + ":" + exception.getMessage() + ". Event=" + changedEvent + ", at "
                + DateFormatUtils.format(new Date(), "yyyyMMdd HH:mm:ss"));

        HandleResult result = new HandleResult();
        result.setIgnoreFailEvent(false);
        return result;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

}
