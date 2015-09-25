package com.dianping.puma.storage.channel;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.exception.StorageException;

import java.io.IOException;

public interface EventChannel extends LifeCycle {

    /*
    EventChannel withTables(String... tables);

    String[] getTables();

    EventChannel withTransaction(boolean transaction);

    boolean getTransaction();

    EventChannel withDdl(boolean ddl);

    boolean getDdl();

    EventChannel withDml(boolean dml);

    boolean getDml();*/

    void open(long serverId, String binlogFile, long binlogPosition) throws IOException;

    /**
     * -1 for oldest ; 0 for newest; other for timestamp;
     *
     * @param startTimeStamp
     */
    void open(long startTimeStamp) throws IOException;

    void close();

    Event next() throws StorageException;

    Event next(boolean shouldSleep) throws StorageException;
}
