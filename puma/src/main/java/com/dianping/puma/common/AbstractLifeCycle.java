package com.dianping.puma.common;

public abstract class AbstractLifeCycle implements LifeCycle {

    private volatile boolean stopped = true;

    @Override
    public void start() {
        if (!isStopped()) {
            return;
        }

        doStart();
        stopped = false;
    }

    @Override
    public void stop() {
        if (isStopped()) {
            return;
        }

        stopped = true;
        doStop();
    }

    protected abstract void doStart();

    protected abstract void doStop();

    protected void checkStop() {
        if (isStopped()) {
            throw new RuntimeException("failed, life cycle is already stopped.");
        }
    }

    protected boolean isStopped() {
        return stopped;
    }
}
