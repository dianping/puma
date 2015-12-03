package com.dianping.puma.storage.cache;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.GroupReadDataManager;

import java.io.IOException;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CachedGroupReadDataManager extends GroupReadDataManager {

    private CachedDataStorage.Reader cachedDataManager;

    private Sequence lastMemorySequence;

    private boolean currentIsMemory = false;

    private long lastSwitchTime = 0;

    public CachedGroupReadDataManager(String database) {
        super(database);
    }

    @Override
    public ChangedEvent next() throws IOException {
        if (currentIsMemory) {
            try {
                ChangedEventWithSequence changedEventWithSequence = cachedDataManager.next();
                if (changedEventWithSequence == null) {
                    return null;
                }

                lastMemorySequence = changedEventWithSequence.getSequence();
                return changedEventWithSequence.getChangedEvent();
            } catch (IOException e) {
                open(lastMemorySequence);
                currentIsMemory = false;
                lastSwitchTime = System.currentTimeMillis();
                return next();
            }
        } else {
            Sequence position = position();
            ChangedEvent event = super.next();

            if (System.currentTimeMillis() - lastSwitchTime > 60 * 1000) {
                lastSwitchTime = System.currentTimeMillis();
                currentIsMemory = cachedDataManager.open(position);
            }

            return event;
        }
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
