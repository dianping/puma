package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.GroupReadDataManager;
import com.dianping.puma.storage.index.ReadIndexManager;
import com.dianping.puma.storage.index.SeriesReadIndexManager;
import com.dianping.puma.storage.index.L1IndexKey;
import com.dianping.puma.storage.index.L2IndexValue;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DefaultReadChannel extends AbstractLifeCycle implements ReadChannel {

    private final String database;

    private EventFilterChain eventFilterChain;

    private ReadIndexManager<L1IndexKey, L2IndexValue> readIndexManager;

    private GroupReadDataManager readDataManager;

    protected DefaultReadChannel(String database) {
        this.database = database;

        initFilter(database, Lists.newArrayList("*"), true, false, false);
    }

    protected DefaultReadChannel(String database, List<String> tables, boolean dml, boolean ddl, boolean transaction) {
        this.database = database;

        initFilter(database, tables, dml, ddl, transaction);
    }

    @Override
    protected void doStart() {
        readIndexManager = new SeriesReadIndexManager(database);
        readIndexManager.start();

        readDataManager = new GroupReadDataManager(database);
        readDataManager.start();
    }

    @Override
    protected void doStop() {
        readIndexManager.stop();
        readDataManager.stop();
    }

    @Override
    public void openOldest() throws IOException {
        L2IndexValue l2IndexValue = readIndexManager.findOldest();
        if (l2IndexValue == null) {
            throw new IOException("failed to open oldest.");
        }
        Sequence sequence = l2IndexValue.getSequence();
        readDataManager.open(new Sequence(sequence));
    }

    @Override
    public void openLatest() throws IOException {
        L2IndexValue l2IndexValue = readIndexManager.findLatest();
        if (l2IndexValue == null) {
            throw new IOException("failed to open latest.");
        }
        Sequence sequence = l2IndexValue.getSequence();
        readDataManager.open(new Sequence(sequence));
    }

    @Override
    public void open(BinlogInfo binlogInfo) throws IOException {
        L2IndexValue l2IndexValue = readIndexManager.find(new L1IndexKey(binlogInfo));
        if (l2IndexValue == null) {
            throw new IOException("failed to open.");
        }
        Sequence sequence = l2IndexValue.getSequence();
        readDataManager.open(new Sequence(sequence));
    }

    @Override
    public ChangedEvent next() throws IOException {
        while (true) {
            ChangedEvent binlogEvent = readDataManager.next();
            if (binlogEvent == null) {
                Uninterruptibles.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
                continue;
            }

            if (!eventFilterChain.doNext(binlogEvent)) {
                continue;
            }

            return binlogEvent;
        }
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
