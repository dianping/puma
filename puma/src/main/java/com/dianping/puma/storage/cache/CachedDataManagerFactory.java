package com.dianping.puma.storage.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public final class CachedDataManagerFactory {
    private CachedDataManagerFactory() {
    }

    private static final Map<String, AtomicInteger> READER_COUNTER = new HashMap<String, AtomicInteger>();
    private static final Map<String, AtomicInteger> WRITER_COUNTER = new HashMap<String, AtomicInteger>();
    private static final Map<String, CachedDataManager> MANAGER_MAP = new HashMap<String, CachedDataManager>();

    public synchronized static CachedDataManager getReadCachedDataManager(String database) {
        if (!READER_COUNTER.containsKey(database)) {
            READER_COUNTER.put(database, new AtomicInteger());
        }
        READER_COUNTER.get(database).incrementAndGet();
        return getCachedDataManager(database);
    }

    public synchronized static void releaseReadCachedDataManager(String database) {
        READER_COUNTER.get(database).decrementAndGet();
    }

    public synchronized static CachedDataManager getWriteCachedDataManager(String database) {
        if (!WRITER_COUNTER.containsKey(database)) {
            WRITER_COUNTER.put(database, new AtomicInteger());
        }
        WRITER_COUNTER.get(database).incrementAndGet();
        return getCachedDataManager(database);
    }

    public synchronized static void releaseWriteCachedDataManager(String database) {
        WRITER_COUNTER.get(database).decrementAndGet();
    }

    private static CachedDataManager getCachedDataManager(String database) {
        if (!MANAGER_MAP.containsKey(database)) {
            MANAGER_MAP.put(database, new CachedDataManager());
        }
        CachedDataManager manager = MANAGER_MAP.get(database);

        int readCount = READER_COUNTER.get(database) == null ? 0 : READER_COUNTER.get(database).get();
        int writeCount = WRITER_COUNTER.get(database) == null ? 0 : WRITER_COUNTER.get(database).get();
        if (readCount > 0 && writeCount > 0) {
            manager.start();
        } else {
            manager.stop();
        }

        return manager;
    }
}
