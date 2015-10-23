package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.EOFException;
import java.io.IOException;

public final class GroupReadDataManager extends AbstractLifeCycle implements ReadDataManager<DataKeyImpl, DataValueImpl> {

	private final String database;

	private DataManagerFinder dataManagerFinder;

	private ReadDataManager<DataKeyImpl, DataValueImpl> readDataManager;

	public GroupReadDataManager(String database) {
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

		if (readDataManager != null) {
			readDataManager.stop();
		}
	}

	@Override
	public DataKeyImpl position() {
		checkStop();

		return readDataManager == null ? null : readDataManager.position();
	}

	@Override
	public void open(DataKeyImpl dataKey) throws IOException {
		checkStop();

		readDataManager = dataManagerFinder.findSlaveReadDataManager(dataKey);
		if (readDataManager == null) {
			readDataManager = dataManagerFinder.findMasterReadDataManager(dataKey);
			if (readDataManager == null) {
				throw new IOException("failed to open group read data manager.");
			}
		}

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

				readDataManager = dataManagerFinder.findNextSlaveReadDataManager(dataKey);
				if (readDataManager == null) {
					readDataManager = dataManagerFinder.findNextMasterReadDataManager(dataKey);
					if (readDataManager == null) {
						throw new IOException("failed to open next read data manager.", eof);
					}
				}

				readDataManager.start();
			}
		}
	}
}
