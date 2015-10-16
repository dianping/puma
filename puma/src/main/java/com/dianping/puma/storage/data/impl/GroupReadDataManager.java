package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.data.DataManagerFinder;
import com.dianping.puma.storage.data.ReadDataManager;

import java.io.EOFException;
import java.io.IOException;

public class GroupReadDataManager extends AbstractLifeCycle implements ReadDataManager<DataKeyImpl, DataValueImpl> {

	private final String database;

	private final String masterBaseDir = "/data/appdatas/puma/storage/master/";

	private final String slaveBaseDir = "/data/appdatas/puma/storage/slave";

	private DataManagerFinder dataManagerFinder;

	private ReadDataManager<DataKeyImpl, DataValueImpl> readDataManager;

	public GroupReadDataManager(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		dataManagerFinder = new GroupDataManagerFinder(database, masterBaseDir, slaveBaseDir);
		dataManagerFinder.start();
	}

	@Override
	protected void doStop() {
		dataManagerFinder.stop();

		if (readDataManager != null) {
			readDataManager.stop();
		}
	}

	@Override
	public DataKeyImpl position() {
		checkStop();

		return (readDataManager == null) ? null : readDataManager.position();
	}

	@Override
	public void open(DataKeyImpl dataKey) throws IOException {
		checkStop();

		readDataManager = dataManagerFinder.findReadDataBucket(dataKey);
		readDataManager.open(dataKey);
	}

	@Override
	public DataValueImpl next() throws IOException {
		checkStop();

		while (true) {
			try {
				return readDataManager.next();
			} catch (EOFException eof) {
				DataKeyImpl dataKey = readDataManager.position();
				readDataManager.stop();

				readDataManager = dataManagerFinder.findNextReadDataBucket(dataKey);
				if (readDataManager == null) {
					throw new IOException("failed to find next read data bucket.");
				}
				readDataManager.start();
			}
		}
	}
}
