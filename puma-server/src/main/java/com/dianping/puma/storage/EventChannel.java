package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.exception.StorageException;

public interface EventChannel {

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

	public void open(long serverId, String binlogFile, long binlogPosition) throws IOException;

	/**
	 * -1 for oldest ; 0 for newest; other for timestamp;
	 * 
	 * @param startTimeStamp
	 */
	public void open(long startTimeStamp) throws IOException;

	public void close();

	public Event next() throws StorageException;

	public Event next(boolean shouldSleep) throws StorageException;
}
