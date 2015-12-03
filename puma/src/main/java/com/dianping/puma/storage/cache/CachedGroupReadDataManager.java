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

    private CachedDataManager cachedDataManager;

    private Sequence memoryCurrentKey;

    private boolean memoryIsEmpty = false;

    private boolean currentIsMemory = false;

    private long lastSwitchTime = 0;

    public CachedGroupReadDataManager(String database) {
        super(database);
    }

    @Override
    public ChangedEvent next() throws IOException {
        ChangedEvent event = null;
        if (currentIsMemory) {
            if (!memoryIsEmpty) {
                event = cachedDataManager.get(memoryCurrentKey);
                if (event == null) {
                    open(memoryCurrentKey);
                    currentIsMemory = false;
                    memoryIsEmpty = false;
                    return next();
                }
            }

            Sequence positon = cachedDataManager.nextPosition(memoryCurrentKey);
            if (positon == null) {
                memoryIsEmpty = true;
            } else {
                memoryIsEmpty = false;
                memoryCurrentKey = positon;
            }

            return event;
        } else {
            Sequence position = position();
            event = super.next();

            if (System.currentTimeMillis() - lastSwitchTime > 10 * 60 * 1000) {
                Sequence nextPosition = cachedDataManager.nextPosition(position);
                if (nextPosition != null) {
                    lastSwitchTime = System.currentTimeMillis();
                    currentIsMemory = true;
                    memoryCurrentKey = nextPosition;
                }
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
