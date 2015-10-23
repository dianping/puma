package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.filesystem.FileSystem;

import java.io.File;
import java.io.IOException;

public final class SeriesIndexManagerFinder extends AbstractLifeCycle implements IndexManagerFinder {

	private String database;

	public SeriesIndexManagerFinder(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	public ReadIndexManager<L1IndexKey, L1IndexValue> findL1ReadIndexManager() throws IOException {
		File file = FileSystem.visitL1IndexFile(database);
		return IndexManagerFactory.newL1SingleReadIndexManager(file.getAbsolutePath());
	}

	@Override
	public ReadIndexManager<L2IndexKey, L2IndexValue> findL2ReadIndexManager(L1IndexValue l1IndexValue)
			throws IOException {
		int date = l1IndexValue.getSequence().getCreationDate();
		int number = l1IndexValue.getSequence().getNumber();
		File file = FileSystem.visitL2IndexFile(database, date, number);
		return IndexManagerFactory.newL2SingleReadIndexManager(file.getAbsolutePath());
	}

	@Override
	public WriteIndexManager<L1IndexKey, L1IndexValue> findL1WriteIndexManager() throws IOException {
		File file = FileSystem.visitL1IndexFile(database);
		return IndexManagerFactory.newL1SingleWriteIndexManager(file.getAbsolutePath());
	}

	@Override
	public WriteIndexManager<L2IndexKey, L2IndexValue> findNextL2WriteIndexManager() throws IOException {
		File file = FileSystem.nextL2IndexFile(database);
		return IndexManagerFactory.newL2SingleWriteIndexManager(file.getAbsolutePath());
	}
}
