package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.utils.DateUtils;

import java.io.IOException;

public final class SeriesWriteIndexManager extends AbstractLifeCycle implements WriteIndexManager<BinlogInfo, Sequence> {

    private String database;

    private L1SingleWriteIndexManager l1WriteIndexManager;

    private L2SingleWriteIndexManager l2WriteIndexManager;

    private int lastDate;

    public SeriesWriteIndexManager(String database) {
        this.database = database;
    }

    @Override
    protected void doStart() {
    }

    @Override
    protected void doStop() {
        if (l1WriteIndexManager != null) {
            l1WriteIndexManager.stop();
        }

        if (l2WriteIndexManager != null) {
            l2WriteIndexManager.stop();
        }
    }

    @Override
    public void append(BinlogInfo binlogInfo, Sequence sequence) throws IOException {
        checkStop();

        if (needPage()) {
            page(binlogInfo, sequence);
            Sequence l2Sequence = l2WriteIndexManager.position();
            l2WriteIndexManager.append(binlogInfo, sequence);
            l2WriteIndexManager.flush();
            l1WriteIndexManager.append(binlogInfo, l2Sequence);
            l1WriteIndexManager.flush();
            return;
        }

        l2WriteIndexManager.append(binlogInfo, sequence);
    }

    @Override
    public void flush() throws IOException {
        checkStop();

        if (l1WriteIndexManager != null) {
            l1WriteIndexManager.flush();
        }

        if (l2WriteIndexManager != null) {
            l2WriteIndexManager.flush();
        }
    }

    @Override
    public Sequence position() {
        return null;
    }

    protected boolean needPage() {
        return l2WriteIndexManager == null ||
                !l2WriteIndexManager.hasRemainingForWrite() ||
                lastDate != getNowInteger();
    }

    protected int getNowInteger() {
        return DateUtils.getNowInteger();
    }

    /**
     * Explicitly page index bucket, call it when paging.
     *
     * @param binlogInfo key of l1 index.
     * @param sequence   value of l2 index.
     * @throws IOException
     */
    protected void page(BinlogInfo binlogInfo, Sequence sequence) throws IOException {
        if (l1WriteIndexManager == null) {
            l1WriteIndexManager = SeriesIndexManagerFinder.findL1WriteIndexManager(database);
            l1WriteIndexManager.start();
        }

        // Flush l2 index before paging.
        if (l2WriteIndexManager != null) {
            l2WriteIndexManager.flush();
        }

        l2WriteIndexManager = SeriesIndexManagerFinder.findNextL2WriteIndexManager(database);
        l2WriteIndexManager.start();

        lastDate = sequence.getCreationDate();
    }
}
