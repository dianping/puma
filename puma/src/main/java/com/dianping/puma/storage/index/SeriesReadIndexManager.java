package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public final class SeriesReadIndexManager extends AbstractLifeCycle
		implements ReadIndexManager<L1IndexKey, L2IndexValue> {

	private String database;

	private IndexManagerFinder indexManagerFinder;

	private ReadIndexManager<L1IndexKey, L1IndexValue> l1ReadIndexManager;

	private ReadIndexManager<L2IndexKey, L2IndexValue> l2ReadIndexManager;

	public SeriesReadIndexManager(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		indexManagerFinder = new SeriesIndexManagerFinder(database);
		indexManagerFinder.start();
	}

	@Override
	protected void doStop() {
		if (indexManagerFinder != null) {
			indexManagerFinder.stop();
		}

		if (l1ReadIndexManager != null) {
			l1ReadIndexManager.stop();
		}

		if (l2ReadIndexManager != null) {
			l2ReadIndexManager.stop();
		}
	}

	@Override
	public L2IndexValue findOldest() throws IOException {
		checkStop();

		if (l1ReadIndexManager == null) {
			l1ReadIndexManager = indexManagerFinder.findL1ReadIndexManager();
			l1ReadIndexManager.start();
		}

		L1IndexValue l1IndexValue = l1ReadIndexManager.findOldest();
		l2ReadIndexManager = indexManagerFinder.findL2ReadIndexManager(l1IndexValue);
		l2ReadIndexManager.start();
		return l2ReadIndexManager.findOldest();
	}

	@Override
	public L2IndexValue findLatest() throws IOException {
		checkStop();

		if (l1ReadIndexManager == null) {
			l1ReadIndexManager = indexManagerFinder.findL1ReadIndexManager();
			l1ReadIndexManager.start();
		}

		L1IndexValue l1IndexValue = l1ReadIndexManager.findLatest();
		l2ReadIndexManager = indexManagerFinder.findL2ReadIndexManager(l1IndexValue);
		return l2ReadIndexManager.findLatest();
	}

	@Override
	public L2IndexValue find(L1IndexKey l1IndexKey) throws IOException {
		checkStop();

		if (l1ReadIndexManager == null) {
			l1ReadIndexManager = indexManagerFinder.findL1ReadIndexManager();
			l1ReadIndexManager.start();
		}

		L1IndexValue l1IndexValue = l1ReadIndexManager.find(l1IndexKey);
		l2ReadIndexManager = indexManagerFinder.findL2ReadIndexManager(l1IndexValue);
		return l2ReadIndexManager.find(new L2IndexKey(l1IndexKey));
	}
}
