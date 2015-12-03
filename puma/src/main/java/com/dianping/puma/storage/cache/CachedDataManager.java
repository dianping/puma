package com.dianping.puma.storage.cache;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CachedDataManager implements LifeCycle {

    private volatile boolean started;

    public void append(Sequence sequence, ChangedEvent dataValue) {
        if (!started) {
            return;
        }

        //todo:
    }

    public Sequence nextPosition(Sequence sequence){
        return null;
    }

    public ChangedEvent get(Sequence sequence) throws IOException {
        if (!started) {

        }

        return null;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }
}
