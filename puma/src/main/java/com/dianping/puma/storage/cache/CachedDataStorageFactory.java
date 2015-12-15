package com.dianping.puma.storage.cache;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
class CachedDataStorageFactory {
    private static volatile CachedDataStorageFactory instance;

    protected CachedDataStorageFactory() {
    }

    static CachedDataStorageFactory getInstance() {
        if (instance == null) {
            synchronized (CachedDataStorageFactory.class) {
                if (instance == null) {
                    instance = new CachedDataStorageFactory();
                }
            }
        }
        return instance;
    }

    private final Map<String, AtomicInteger> READER_COUNTER = new HashMap<String, AtomicInteger>();
    private final Map<String, AtomicInteger> WRITER_COUNTER = new HashMap<String, AtomicInteger>();
    private final Map<String, CachedDataStorage> MANAGER_MAP = new HashMap<String, CachedDataStorage>();

    final synchronized CachedDataStorage.Reader getReadCachedDataManager(String database) {
        if (!READER_COUNTER.containsKey(database)) {
            READER_COUNTER.put(database, new AtomicInteger());
        }
        READER_COUNTER.get(database).incrementAndGet();
        return getCachedDataManager(database).createReader();
    }

    final synchronized void releaseReadCachedDataManager(String database) {
        Preconditions.checkState(READER_COUNTER.get(database).get() > 0, "No reader allocated:" + database);

        READER_COUNTER.get(database).decrementAndGet();
        getCachedDataManager(database);
    }

    final synchronized CachedDataStorage getWriteCachedDataManager(String database) {
        if (!WRITER_COUNTER.containsKey(database)) {
            WRITER_COUNTER.put(database, new AtomicInteger());
        }

        Preconditions.checkState(WRITER_COUNTER.get(database).get() == 0, "Not allow multi writer:" + database);

        WRITER_COUNTER.get(database).incrementAndGet();
        return getCachedDataManager(database);
    }

    final synchronized void releaseWriteCachedDataManager(String database) {
        Preconditions.checkState(WRITER_COUNTER.get(database).get() == 1, "No writer allocated:" + database);

        WRITER_COUNTER.get(database).decrementAndGet();
        getCachedDataManager(database);
    }

    private CachedDataStorage getCachedDataManager(String database) {
        if (!MANAGER_MAP.containsKey(database)) {
            MANAGER_MAP.put(database, initCachedDataStorage());
        }
        CachedDataStorage manager = MANAGER_MAP.get(database);

        int readCount = READER_COUNTER.get(database) == null ? 0 : READER_COUNTER.get(database).get();
        int writeCount = WRITER_COUNTER.get(database) == null ? 0 : WRITER_COUNTER.get(database).get();
        if (readCount > 0 && writeCount > 0) {
            manager.start();
        } else {
            manager.stop();
        }

        return manager;
    }

    protected CachedDataStorage initCachedDataStorage() {
        return new CachedDataStorage();
    }
}
