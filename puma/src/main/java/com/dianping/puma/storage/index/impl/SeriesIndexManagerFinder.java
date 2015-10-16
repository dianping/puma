package com.dianping.puma.storage.index.impl;

import com.dianping.puma.storage.index.IndexManagerFinder;
import com.dianping.puma.storage.index.ReadIndexManager;
import com.dianping.puma.storage.index.WriteIndexManager;

public class SeriesIndexManagerFinder implements IndexManagerFinder {

	private String database;

	private String l1IndexBaseDir = "/data/appdatas/puma/binlogIndex/l1Index/";

	private String l2IndexBaseDir = "/data/appdatas/puma/binlogIndex/l2Index/";

	public SeriesIndexManagerFinder(String database, String l1IndexBaseDir, String l2IndexBaseDir) {
		this.database = database;
		this.l1IndexBaseDir = l1IndexBaseDir;
		this.l2IndexBaseDir = l2IndexBaseDir;
	}

	@Override public void start() {

	}

	@Override public void stop() {

	}

	@Override public ReadIndexManager<L1IndexKey, L1IndexValue> findL1ReadIndexManager() {
		return null;
	}

	@Override public ReadIndexManager<L2IndexKey, L2IndexValue> findL2ReadIndexManager(L1IndexValue l1IndexValue) {
		return null;
	}

	@Override public WriteIndexManager<L1IndexKey, L1IndexValue> findL1WriteIndexManager() {
		return null;
	}

	@Override public WriteIndexManager<L2IndexKey, L2IndexValue> findNextL2WriteIndexManager() {
		return null;
	}
}
