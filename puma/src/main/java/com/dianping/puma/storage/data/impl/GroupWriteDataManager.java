package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.data.DataManagerFinder;
import com.dianping.puma.storage.data.WriteDataManager;

import java.io.IOException;

public class GroupWriteDataManager extends AbstractLifeCycle implements WriteDataManager<DataKeyImpl, DataValueImpl> {

	private String database;

	private String masterBaseDir = "/data/appdatas/puma/storage/master/";

	private String slaveBaseDir = "/data/appdatas/puma/storage/slave/";

	private DataManagerFinder dataManagerFinder;

	private WriteDataManager<DataKeyImpl, DataValueImpl> writeDataManager;

	public GroupWriteDataManager(String database) {
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

		if (writeDataManager != null) {
			writeDataManager.stop();
		}
	}

	@Override
	public void append(DataKeyImpl dataKeyImpl, DataValueImpl dataValueImpl) throws IOException {
		checkStop();

		createWriteDataManagerIfNeeded();

		writeDataManager.append(dataKeyImpl, dataValueImpl);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		createWriteDataManagerIfNeeded();

		writeDataManager.flush();
	}

	@Override
	public boolean hasRemainingForWrite() {
		return true;
	}

	public void pageAppend(DataKeyImpl dataKeyImpl, DataValueImpl dataValueImpl) throws IOException {

	}

	public boolean hasRemainingForWriteOnCurrentPage() {
		checkStop();

		return writeDataManager != null && writeDataManager.hasRemainingForWrite();
	}

	protected void page() throws IOException {
		checkStop();

		writeDataManager = dataManagerFinder.genNextWriteDataBucket();
		if (writeDataManager == null) {
			throw new IOException("failed to generate the next write data bucket.");
		}
	}

	protected void createWriteDataManagerIfNeeded() throws IOException {
		if (writeDataManager == null) {
			page();
		}
	}
}
