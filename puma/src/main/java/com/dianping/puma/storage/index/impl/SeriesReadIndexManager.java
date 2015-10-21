package com.dianping.puma.storage.index.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.index.IndexManagerFinder;
import com.dianping.puma.storage.index.ReadIndexManager;

import java.io.IOException;

public final class SeriesReadIndexManager extends AbstractLifeCycle
		implements ReadIndexManager<L1IndexKey, L2IndexValue> {

	private String database;

	private String l1IndexBaseDir = "/data/appdatas/puma/binlogIndex/l1Index/";

	private String l2IndexBaseDir = "/data/appdatas/puma/binlogIndex/l2Index/";

	private IndexManagerFinder indexManagerFinder;

	private ReadIndexManager<L1IndexKey, L1IndexValue> l1ReadIndexManager;

	private ReadIndexManager<L2IndexKey, L2IndexValue> l2ReadIndexManager;

	public SeriesReadIndexManager(String database) {
		this.database = database;
	}

	public SeriesReadIndexManager(String database, String l1IndexBaseDir, String l2IndexBaseDir) {
		this.database = database;
		this.l1IndexBaseDir = l1IndexBaseDir;
		this.l2IndexBaseDir = l2IndexBaseDir;
	}

	@Override
	protected void doStart() {
		indexManagerFinder = new SeriesIndexManagerFinder(database, l1IndexBaseDir, l2IndexBaseDir);
		indexManagerFinder.start();
	}

	@Override
	protected void doStop() {
		indexManagerFinder.stop();

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
