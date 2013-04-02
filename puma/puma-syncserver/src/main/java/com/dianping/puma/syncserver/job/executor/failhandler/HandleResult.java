package com.dianping.puma.syncserver.job.executor.failhandler;

public class HandleResult {
    private boolean ignoreFailEvent = false;

    public boolean isIgnoreFailEvent() {
        return ignoreFailEvent;
    }

    public void setIgnoreFailEvent(boolean ignoreFailEvent) {
        this.ignoreFailEvent = ignoreFailEvent;
    }

}
