package com.dianping.puma.storage.cache;

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

    private final CachedDataStorageFactory cachedDataStorageFactory;

    private final String database;

    private Sequence lastSequence;

    private boolean currentIsMemory = false;

    private long lastSwitchTime = 0;

    public CachedGroupReadDataManager(String database) {
        this(database, CachedDataStorageFactory.getInstance(), new GroupReadDataManager(database));
    }

    protected CachedGroupReadDataManager(String database, CachedDataStorageFactory cachedDataStorageFactory, GroupReadDataManager groupReadDataManager) {
        this.database = database;
        this.cachedDataStorageFactory = cachedDataStorageFactory;
        this.groupReadDataManager = groupReadDataManager;
    }

    @Override
    public Sequence position() {
        throw new IllegalAccessError();
        //by Dozer
        //这里不能用 return lastSequence,因为整个系统 position 的语义是下一个位置,而这里是上一个位置,所以会造成误解,引发bug.
        //目前没有方法调用这里的position,所以就先不实现了,等需要调用的时候再去实现
    }

    @Override
    public void open(Sequence dataKey) throws IOException {
        currentIsMemory = cachedDataManager.open(dataKey);
        if (!currentIsMemory) {
            groupReadDataManager.open(dataKey);
        }

        lastSequence = dataKey;
    }

    @Override
    public ChangedEvent next() throws IOException {
        if (currentIsMemory) {
            try {
                return memoryNext();
            } catch (IOException e) {
                switchToFile();
                return next();
            }
        } else {
            ChangedEvent event = fileNext();
            trySwitchToMemory();
            return event;
        }
    }

    protected ChangedEvent fileNext() throws IOException {
        Sequence position = groupReadDataManager.position();
        ChangedEvent event = groupReadDataManager.next();

        if (event != null) {
            lastSequence = position;
        } else {
            //如果一直是Null,说明已经到达最新数据了,可以尽早尝试切换
            lastSwitchTime--;
        }
        return event;
    }

    protected ChangedEvent memoryNext() throws IOException {
        ChangedEventWithSequence changedEventWithSequence = cachedDataManager.next();
        if (changedEventWithSequence == null) {
            return null;
        } else {
            lastSequence = changedEventWithSequence.getSequence();
            return changedEventWithSequence.getChangedEvent();
        }
    }

    @Override
    public String getStorageMode() {
        return currentIsMemory ? "Memory" : "File";
    }

    protected void trySwitchToMemory() {
        if (System.currentTimeMillis() - lastSwitchTime > 60 * 1000) {
            lastSwitchTime = System.currentTimeMillis();
            currentIsMemory = cachedDataManager.open(lastSequence);
        }
    }

    protected void switchToFile() throws IOException {
        groupReadDataManager.open(lastSequence);
        currentIsMemory = false;
        lastSwitchTime = System.currentTimeMillis();
    }

    @Override
    public void start() {
        groupReadDataManager.start();
        this.cachedDataManager = cachedDataStorageFactory.getReadCachedDataManager(database);
    }

    @Override
    public void stop() {
        this.cachedDataManager = null;
        cachedDataStorageFactory.releaseReadCachedDataManager(database);
        groupReadDataManager.stop();
    }
}
