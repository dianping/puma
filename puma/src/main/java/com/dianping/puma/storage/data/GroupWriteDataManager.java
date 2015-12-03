package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.utils.DateUtils;

import java.io.IOException;

public class GroupWriteDataManager extends AbstractLifeCycle
        implements WriteDataManager<Sequence, ChangedEvent> {

    protected String database;

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

        if (needPage()) {
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

    protected boolean needPage() {
        return writeDataManager == null ||
                !writeDataManager.hasRemainingForWrite() ||
                writeDataManager.position().getCreationDate() != getNowInteger();
    }

    protected int getNowInteger() {
        return DateUtils.getNowInteger();
    }

    protected void page() throws IOException {
        if (writeDataManager != null) {
            writeDataManager.flush();
        }

        writeDataManager = GroupDataManagerFinder.findNextMasterWriteDataManager(database);
    }
}
