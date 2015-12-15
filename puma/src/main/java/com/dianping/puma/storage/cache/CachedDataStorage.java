package com.dianping.puma.storage.cache;

import com.dianping.puma.common.LifeCycle;
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

    private volatile long dataVersion = 0;

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
        dataVersion++;
        nextWriteIndex = 0;
    }

    public Reader createReader() {
        return new Reader();
    }

    public class Reader {

        private Reader() {
        }

        private volatile long readDataVersion = -1;

        private volatile long nextReadIndex = 0;

        public boolean open(Sequence sequence) {
            if (!started) {
                return false;
            }

            for (long k = nextWriteIndex - 1; (nextWriteIndex - k < CACHED_SIZE) && k >= 0; k--) {
                if (sequence.equals(data[(int) (k % CACHED_SIZE)].getSequence())) {
                    nextReadIndex = k;
                    readDataVersion = dataVersion;
                    return true;
                }
            }
            return false;
        }

        public ChangedEventWithSequence next() throws IOException {
            if (!started || readDataVersion != dataVersion) {
                throw new IOException("data outdated");
            }

            if (nextReadIndex >= nextWriteIndex) {
                return null;
            }

            if (nextReadIndex <= nextWriteIndex - CACHED_SIZE) {
                throw new IOException("data outdated");
            }

            ChangedEventWithSequence event = data[(int) (nextReadIndex % CACHED_SIZE)];

            if (nextReadIndex <= nextWriteIndex - CACHED_SIZE) {
                throw new IOException("data outdated");
            } else {
                nextReadIndex++;
                return event;
            }
        }
    }
}
