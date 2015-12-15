package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public final class SeriesReadIndexManager extends AbstractLifeCycle
        implements ReadIndexManager<BinlogInfo, Sequence> {

    private String database;

    private L1SingleReadIndexManager l1ReadIndexManager;

    private L2SingleReadIndexManager l2ReadIndexManager;

    public SeriesReadIndexManager(String database) {
        this.database = database;
    }

    @Override
    protected void doStart() {
    }

    @Override
    protected void doStop() {
        if (l1ReadIndexManager != null) {
            l1ReadIndexManager.stop();
        }

        if (l2ReadIndexManager != null) {
            l2ReadIndexManager.stop();
        }
    }

    @Override
    public Sequence findOldest() throws IOException {
        checkStop();

        doStop();

        l1ReadIndexManager = SeriesIndexManagerFinder.findL1ReadIndexManager(database);
        l1ReadIndexManager.start();
        Sequence sequence = l1ReadIndexManager.findOldest();

        l2ReadIndexManager = SeriesIndexManagerFinder.findL2ReadIndexManager(database, sequence);
        l2ReadIndexManager.start();
        return l2ReadIndexManager.findOldest();
    }

    @Override
    public Sequence findLatest() throws IOException {
        checkStop();

        doStop();

        l1ReadIndexManager = SeriesIndexManagerFinder.findL1ReadIndexManager(database);
        l1ReadIndexManager.start();
        Sequence sequence = l1ReadIndexManager.findLatest();

        l2ReadIndexManager = SeriesIndexManagerFinder.findL2ReadIndexManager(database, sequence);
        l2ReadIndexManager.start();
        return l2ReadIndexManager.findLatest();
    }

    @Override
    public Sequence find(BinlogInfo binlogInfo) throws IOException {
        checkStop();

        doStop();

        l1ReadIndexManager = SeriesIndexManagerFinder.findL1ReadIndexManager(database);
        l1ReadIndexManager.start();

        Sequence sequence = l1ReadIndexManager.find(binlogInfo);
        if (sequence == null) {
            l1ReadIndexManager.stop();
            l1ReadIndexManager.start();
            sequence = l1ReadIndexManager.findLatest();
            if (sequence == null) {
                throw new IOException("failed to find binlog info.");
            }
        }

        l2ReadIndexManager = SeriesIndexManagerFinder.findL2ReadIndexManager(database, sequence);
        l2ReadIndexManager.start();
        return l2ReadIndexManager.find(binlogInfo);
    }
}
