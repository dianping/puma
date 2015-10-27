package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public final class GroupWriteDataManager extends AbstractLifeCycle
        implements WriteDataManager<Sequence, ChangedEvent> {

    private String database;

    private SingleWriteDataManager writeDataManager;

    public GroupWriteDataManager(String database) {
        this.database = database;
    }

    @Override
    protected void doStart() {
    }

    @Override
    protected void doStop() {
        if (writeDataManager != null) {
            writeDataManager.stop();
        }
    }

    @Override
    public Sequence append(ChangedEvent binlogEvent) throws IOException {
        checkStop();

        if (!hasRemainingForWriteOnCurrentPage()) {
            page();
        }

        return writeDataManager.append(binlogEvent);
    }

    @Override
    public void flush() throws IOException {
        checkStop();

        if (writeDataManager != null) {
            writeDataManager.flush();
        }
    }

    @Override
    public Sequence position() {
        checkStop();

        return writeDataManager.position();
    }

    protected boolean hasRemainingForWriteOnCurrentPage() {
        return writeDataManager != null && writeDataManager.hasRemainingForWrite();
    }

    protected void page() throws IOException {
        if (writeDataManager != null) {
            writeDataManager.flush();
        }

        writeDataManager = GroupDataManagerFinder.findNextMasterWriteDataManager(database);
        writeDataManager.start();
    }
}
