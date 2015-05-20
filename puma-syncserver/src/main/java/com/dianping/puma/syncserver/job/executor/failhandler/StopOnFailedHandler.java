package com.dianping.puma.syncserver.job.executor.failhandler;

import com.dianping.puma.core.entity.BaseSyncTask;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.syncserver.job.executor.AbstractTaskExecutor;

public class StopOnFailedHandler implements Handler {

    public static final String  NAME = "StopOnFailed";

    private static final Logger LOG  = LoggerFactory.getLogger(StopOnFailedHandler.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public HandleResult handle(HandleContext context) {
        AbstractTaskExecutor executor = context.getExecutor();
        BaseSyncTask task = context.getTask();
        Exception exception = context.getException();
        ChangedEvent changedEvent = context.getChangedEvent();
        CircularFifoBuffer lastEvents = context.getLastEvents();

        //executor.fail(task.getPumaTaskName() + "->" + task.getDstDBInstanceName() + ":" + exception.getMessage() + ". Event=" + changedEvent);
        LOG.info("Print last 10 row change events: " + lastEvents.toString());

        HandleResult result = new HandleResult();
        result.setIgnoreFailEvent(false);
        return result;
    }

}
