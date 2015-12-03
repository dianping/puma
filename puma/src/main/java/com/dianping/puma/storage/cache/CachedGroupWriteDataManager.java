package com.dianping.puma.storage.cache;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.GroupWriteDataManager;

import java.io.IOException;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CachedGroupWriteDataManager extends GroupWriteDataManager {

    private CachedDataStorage cachedDataStorage;

    public CachedGroupWriteDataManager(String database) {
        super(database);
    }

    @Override
    public Sequence append(ChangedEvent binlogEvent) throws IOException {
        Sequence sequence = super.append(binlogEvent);
        cachedDataStorage.append(new ChangedEventWithSequence(binlogEvent, sequence));
        return sequence;
    }

    @Override
    protected void doStart() {
        super.doStart();
        this.cachedDataStorage = CachedDataManagerFactory.getWriteCachedDataManager(database);
    }

    @Override
    protected void doStop() {
        this.cachedDataStorage = null;
        CachedDataManagerFactory.releaseWriteCachedDataManager(database);
        super.doStop();
    }
}
