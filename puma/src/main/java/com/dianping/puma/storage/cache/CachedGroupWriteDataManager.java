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

    private CachedDataManager cachedDataManager;

    public CachedGroupWriteDataManager(String database) {
        super(database);
    }

    @Override
    public Sequence append(ChangedEvent binlogEvent) throws IOException {
        Sequence sequence = super.append(binlogEvent);
        cachedDataManager.append(sequence, binlogEvent);
        return sequence;
    }

    @Override
    protected void doStart() {
        super.doStart();
        this.cachedDataManager = CachedDataManagerFactory.getReadCachedDataManager(database);
    }

    @Override
    protected void doStop() {
        this.cachedDataManager = null;
        CachedDataManagerFactory.releaseReadCachedDataManager(database);
        super.doStop();
    }
}
