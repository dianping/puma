package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public final class GroupWriteDataManager extends AbstractLifeCycle implements WriteDataManager<DataKeyImpl, DataValueImpl> {

	private String database;

	private DataManagerFinder dataManagerFinder;

	private WriteDataManager<DataKeyImpl, DataValueImpl> writeDataManager;

	public GroupWriteDataManager(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		dataManagerFinder = new GroupDataManagerFinder(database);
		dataManagerFinder.start();
	}

	@Override
	protected void doStop() {
		if (dataManagerFinder != null) {
			dataManagerFinder.stop();
		}

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

		writeDataManager = dataManagerFinder.findNextMasterWriteDataManager();
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
