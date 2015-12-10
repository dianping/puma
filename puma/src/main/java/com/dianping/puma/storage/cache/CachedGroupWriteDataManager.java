package com.dianping.puma.storage.cache;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.GroupWriteDataManager;
import com.dianping.puma.storage.data.WriteDataManager;

import java.io.IOException;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CachedGroupWriteDataManager implements WriteDataManager<Sequence, ChangedEvent> {

    private CachedDataStorage cachedDataStorage;

    private final GroupWriteDataManager groupWriteDataManager;

    private final String database;

    public CachedGroupWriteDataManager(String database) {
        this.database = database;
        this.groupWriteDataManager = new GroupWriteDataManager(database);
    }

    @Override
    public Sequence append(ChangedEvent binlogEvent) throws IOException {
        Sequence sequence = groupWriteDataManager.append(binlogEvent);
        cachedDataStorage.append(new ChangedEventWithSequence(binlogEvent, sequence));
        return sequence;
    }

    @Override
    public void flush() throws IOException {
        groupWriteDataManager.flush();
    }

    @Override
    public Sequence position() {
        return groupWriteDataManager.position();
    }

    @Override
    public void start() {
        groupWriteDataManager.start();
        this.cachedDataStorage = CachedDataStorageFactory.getInstance().getWriteCachedDataManager(database);
    }

    @Override
    public void stop() {
        this.cachedDataStorage = null;
        CachedDataStorageFactory.getInstance().releaseWriteCachedDataManager(database);
        groupWriteDataManager.stop();
    }
}
