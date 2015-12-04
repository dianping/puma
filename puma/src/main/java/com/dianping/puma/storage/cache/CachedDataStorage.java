package com.dianping.puma.storage.cache;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CachedDataStorage implements LifeCycle {

    private volatile boolean started;

    private static final int CACHED_SIZE = 5000;

    private final ChangedEventWithSequence[] data = new ChangedEventWithSequence[CACHED_SIZE];

    private volatile long nextWriteIndex = 0;


    public void append(ChangedEventWithSequence dataValue) {
        if (!started) {
            return;
        }

        data[(int) (nextWriteIndex % CACHED_SIZE)] = dataValue;
        nextWriteIndex++;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    public class Reader {
        private volatile long nextReadIndex = 0;

        public boolean open(Sequence sequence) {
            for (long k = nextWriteIndex - 1; (nextWriteIndex - k < CACHED_SIZE) && k >= 0; k--) {
                if (sequence.equals(data[(int) (k % CACHED_SIZE)].getSequence())) {
                    nextReadIndex = k;
                    return true;
                }
            }
            return false;
        }

        public ChangedEventWithSequence next() throws IOException {
            if (!started) {
                throw new IOException("data outdated");
            }

            if (nextReadIndex > nextWriteIndex - 1) {
                return null;
            }

            ChangedEventWithSequence event = data[(int) (nextReadIndex % CACHED_SIZE)];
            if (nextReadIndex <= nextWriteIndex - CACHED_SIZE) {//todo:验证边界条件和并发问题
                throw new IOException("data outdated");
            } else {
                nextReadIndex++;
                return event;
            }
        }
    }
}
