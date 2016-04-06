package com.dianping.puma.common;

import com.dianping.puma.common.exception.PumaException;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public abstract class AbstractPumaLifeCycle implements PumaLifeCycle {

    protected volatile boolean running = false;

    @Override
    public void start() {
        if (running) {
            throw new PumaException("[%s] has been started.", getClass().getName());
        }

        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            throw new PumaException("[%s] has been stopped.", getClass().getName());
        }

        running = false;
    }

    @Override
    public boolean isStart() {
        return running;
    }
}
