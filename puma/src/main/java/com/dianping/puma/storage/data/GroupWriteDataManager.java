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
		try {
			writeDataManager = DataManagerFinder.findNextMasterWriteDataManager(database);
		} catch (IOException io) {
			throw new IllegalStateException("failed to start write data manager.");
		}
	}

	@Override
	protected void doStop() {
		if (writeDataManager != null) {
			writeDataManager.stop();
		}
	}

	@Override
	public void append(ChangedEvent binlogEvent) throws IOException {
		checkStop();

		createWriteDataManagerIfNeeded();

		writeDataManager.append(binlogEvent);
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

	@Override
	public com.dianping.puma.storage.Sequence position() {
		checkStop();

		return writeDataManager.position();
	}

	public void pageAppend(Sequence sequence, DataValueImpl dataValueImpl) throws IOException {

	}

	public boolean hasRemainingForWriteOnCurrentPage() {
		checkStop();

		return writeDataManager != null && writeDataManager.hasRemainingForWrite();
	}

	protected void page() throws IOException {
		checkStop();

		writeDataManager = DataManagerFinder.findNextMasterWriteDataManager(database);
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
