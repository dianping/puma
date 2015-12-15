package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;

import java.io.EOFException;
import java.io.IOException;

public class GroupReadDataManager extends AbstractLifeCycle
        implements ReadDataManager<com.dianping.puma.storage.Sequence, ChangedEvent> {

    protected final String database;

    private SingleReadDataManager readDataManager;

    public GroupReadDataManager(String database) {
        this.database = database;
    }

    @Override
    protected void doStart() {
    }

    @Override
    protected void doStop() {
        if (readDataManager != null) {
            readDataManager.stop();
        }
    }

    @Override
    public Sequence position() {
        checkStop();

        return readDataManager == null ? null : readDataManager.position();
    }

    @Override
    public void open(Sequence sequence) throws IOException {
        checkStop();

        readDataManager = GroupDataManagerFinder.findMasterReadDataManager(database, sequence);
        if (readDataManager == null) {
            throw new IOException("failed to open group read data manager.");
        }
    }

    @Override
    public ChangedEvent next() throws IOException {
        checkStop();

        while (true) {
            try {
                return readDataManager.next();
            } catch (EOFException eof) {
                Sequence dataKey = readDataManager.position();

                SingleReadDataManager tempReadDataManager = GroupDataManagerFinder.findNextMasterReadDataManager(database, dataKey);
                if (tempReadDataManager == null) {
                    return null;
                }

                readDataManager.stop();
                tempReadDataManager.start();

                readDataManager = tempReadDataManager;
            }
        }
    }

    @Override
    public String getStorageMode() {
        return "File";
    }
}
