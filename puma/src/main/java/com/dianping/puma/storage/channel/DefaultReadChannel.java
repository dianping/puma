package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.cache.CachedGroupReadDataManager;
import com.dianping.puma.storage.data.ReadDataManager;
import com.dianping.puma.storage.index.SeriesReadIndexManager;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class DefaultReadChannel extends AbstractLifeCycle implements ReadChannel {

    private final String database;

    private EventFilterChain eventFilterChain;

    private SeriesReadIndexManager readIndexManager;

    private ReadDataManager<Sequence, ChangedEvent> readDataManager;

    private long lastEventTime = 0;

    protected DefaultReadChannel(String database) {
        this.database = database;

        initFilter(database, Lists.newArrayList("*"), true, false, false);
    }

    protected DefaultReadChannel(String database, List<String> tables, boolean dml, boolean ddl, boolean transaction) {
        this.database = database;

        initFilter(database, tables, dml, ddl, transaction);
    }

    @Override
    protected synchronized void doStart() {
        readIndexManager = new SeriesReadIndexManager(database);
        readIndexManager.start();

        readDataManager = new CachedGroupReadDataManager(database);
        readDataManager.start();
    }

    @Override
    protected synchronized void doStop() {
        readIndexManager.stop();
        readDataManager.stop();
    }

    @Override
    public void openOldest() throws IOException {
        Sequence sequence = readIndexManager.findOldest();
        if (sequence == null) {
            throw new IOException("failed to open oldest.");
        }
        readDataManager.open(new Sequence(sequence));
    }

    @Override
    public void openLatest() throws IOException {
        Sequence sequence = readIndexManager.findLatest();
        if (sequence == null) {
            throw new IOException("failed to open latest.");
        }
        readDataManager.open(new Sequence(sequence));
    }

    @Override
    public void open(BinlogInfo binlogInfo) throws IOException {
        Sequence sequence = readIndexManager.find(binlogInfo);
        if (sequence == null) {
            throw new IOException("failed to open.");
        }
        readDataManager.open(new Sequence(sequence));
    }

    @Override
    public ChangedEvent next() throws IOException {
        while (true) {
            ChangedEvent binlogEvent = readDataManager.next();

            if (binlogEvent == null) {
                return null;
            }

            eventFilterChain.reset();
            if (!eventFilterChain.doNext(binlogEvent)) {
                if (System.currentTimeMillis() - lastEventTime > 60 * 60 * 1000 && binlogEvent.getBinlogInfo() != null) {
                    RowChangedEvent heartbeatEvent = new RowChangedEvent();
                    heartbeatEvent.setDmlType(DMLType.NULL);
                    heartbeatEvent.setBinlogInfo(binlogEvent.getBinlogInfo());
                    heartbeatEvent.setDatabase("HeartbeatEvent");
                    heartbeatEvent.setTable("HeartbeatEvent");
                    heartbeatEvent.setColumns(new HashMap<String, RowChangedEvent.ColumnInfo>());
                    lastEventTime = System.currentTimeMillis();
                    return heartbeatEvent;
                } else {
                    continue;
                }
            }

            lastEventTime = System.currentTimeMillis();
            return binlogEvent;
        }
    }

    @Override
    public String getStorageMode() {
        return readDataManager.getStorageMode();
    }

    protected void initFilter(final String database, List<String> tables, boolean dml, boolean ddl, boolean transaction) {
        String[] dts = FluentIterable
                .from(tables)
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String table) {
                        return database + "." + table;
                    }
                })
                .toArray(String.class);
        eventFilterChain = EventFilterChainFactory.createEventFilterChain(ddl, dml, transaction, dts);
    }
}
