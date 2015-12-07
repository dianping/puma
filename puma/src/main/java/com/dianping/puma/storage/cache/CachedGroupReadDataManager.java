package com.dianping.puma.storage.cache;

import com.dianping.cat.Cat;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.GroupReadDataManager;
import com.dianping.puma.storage.data.ReadDataManager;

import java.io.IOException;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CachedGroupReadDataManager implements ReadDataManager<Sequence, ChangedEvent> {

    private CachedDataStorage.Reader cachedDataManager;

    private final GroupReadDataManager groupReadDataManager;

    private Sequence lastMemorySequence;

    private final String database;

    private boolean currentIsMemory = false;

    private long lastSwitchTime = 0;

    public CachedGroupReadDataManager(String database) {
        this.database = database;
        this.groupReadDataManager = new GroupReadDataManager(database);
    }

    @Override
    public Sequence position() {
        return currentIsMemory ? lastMemorySequence : groupReadDataManager.position();
    }

    @Override
    public void open(Sequence dataKey) throws IOException {
        currentIsMemory = cachedDataManager.open(dataKey);
        if (!currentIsMemory) {
            groupReadDataManager.open(dataKey);
        }
    }

    @Override
    public ChangedEvent next() throws IOException {
        if (currentIsMemory) {
            try {
                ChangedEventWithSequence changedEventWithSequence = cachedDataManager.next();
                if (changedEventWithSequence == null) {
                    return null;
                }
                Cat.logEvent("Read", "M");
                lastMemorySequence = changedEventWithSequence.getSequence();
                return changedEventWithSequence.getChangedEvent();
            } catch (IOException e) {
                switchToFile();
                return next();
            }
        } else {
            Sequence position = position();
            ChangedEvent event = groupReadDataManager.next();
            if (event != null) {
                Cat.logEvent("Read", "F");
            }
            trySwitchToMemory(position);
            return event;
        }
    }

    private void trySwitchToMemory(Sequence position) {
        if (System.currentTimeMillis() - lastSwitchTime > 60 * 1000) {
            lastSwitchTime = System.currentTimeMillis();
            currentIsMemory = cachedDataManager.open(position);
        }
    }

    private void switchToFile() throws IOException {
        groupReadDataManager.open(lastMemorySequence);
        currentIsMemory = false;
        lastSwitchTime = System.currentTimeMillis();
    }

    @Override
    public void start() {
        groupReadDataManager.start();
        this.cachedDataManager = CachedDataManagerFactory.getReadCachedDataManager(database);
    }

    @Override
    public void stop() {
        this.cachedDataManager = null;
        CachedDataManagerFactory.releaseReadCachedDataManager(database);
        groupReadDataManager.stop();
    }
}
