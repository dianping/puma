package com.dianping.puma.syncserver.job.executor.failhandler;

import java.util.concurrent.TimeUnit;

public class RetryHandler implements Handler {

    private long sleepTime = 1;

    @Override
    public String getName() {
        return "Retry";
    }

    @Override
    public HandleResult handle(HandleContext context) {
        try {
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
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
