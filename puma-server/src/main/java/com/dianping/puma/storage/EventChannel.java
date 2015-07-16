package com.dianping.puma.storage;

import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.exception.StorageException;

public interface EventChannel {

    /**
     * default value is all;
     *
     * @param transaction
     * @return
     */
    public EventChannel withDatabase(String database);

    /**
     * default value is all;
     *
     * @param transaction
     * @return
     */
    public EventChannel withTables(String... tables);

    /**
     * default value is true;
     *
     * @param transaction
     * @return
     */
    public EventChannel withTransaction(boolean transaction);

    /**
     * default value is false;
     *
     * @param transaction
     * @return
     */
    public EventChannel withDdl(boolean ddl);

    /**
     * default value is true;
     *
     * @param transaction
     * @return
     */
    public EventChannel withDml(boolean dml);

    public void open();

    public void close();

    public Event next() throws StorageException;

    public Event next(boolean emptyReturnNull) throws StorageException;
}
