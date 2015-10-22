package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public class SeriesWriteIndexManager extends AbstractLifeCycle implements WriteIndexManager<L1IndexKey, L2IndexValue> {

	private String database;

	private String l1IndexBaseDir = "/data/appdatas/puma/binlogIndex/l1Index/";

	private String l2IndexBaseDir = "/data/appdatas/puma/binlogIndex/l2Index/";

	private IndexManagerFinder indexManagerFinder;

	private WriteIndexManager<L1IndexKey, L1IndexValue> l1WriteIndexManager;

	private WriteIndexManager<L2IndexKey, L2IndexValue> l2WriteIndexManager;

	public SeriesWriteIndexManager(String database, String l1IndexBaseDir, String l2IndexBaseDir) {
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

		if (l1WriteIndexManager != null) {
			l1WriteIndexManager.stop();
		}

		if (l2WriteIndexManager != null) {
			l2WriteIndexManager.stop();
		}
	}

	@Override
	public void append(L1IndexKey l1IndexKey, L2IndexValue l2IndexValue) throws IOException {
		checkStop();

		if (l1WriteIndexManager == null) {
			l1WriteIndexManager = indexManagerFinder.findL1WriteIndexManager();
			l1WriteIndexManager.start();
		}

		if (l2WriteIndexManager == null) {
			l2WriteIndexManager = indexManagerFinder.findNextL2WriteIndexManager();
			l2WriteIndexManager.start();
		}

		l2WriteIndexManager.append(new L2IndexKey(l1IndexKey.getBinlogInfo()), l2IndexValue);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		if (l1WriteIndexManager == null) {
			l1WriteIndexManager = indexManagerFinder.findL1WriteIndexManager();
			l1WriteIndexManager.start();
		}

		if (l2WriteIndexManager == null) {
			l2WriteIndexManager = indexManagerFinder.findNextL2WriteIndexManager();
			l2WriteIndexManager.start();
		}

		l1WriteIndexManager.flush();
		l2WriteIndexManager.flush();
	}

	/**
	 * Explicitly page index bucket, call it when paging.
	 *
	 * @param l1IndexKey key of l1 index.
	 * @param l2IndexValue value of l2 index.
	 * @throws IOException
	 */
	public void pageAppend(L1IndexKey l1IndexKey, L2IndexValue l2IndexValue) throws IOException {
		checkStop();

		if (l1WriteIndexManager == null) {
			l1WriteIndexManager = indexManagerFinder.findL1WriteIndexManager();
			l1WriteIndexManager.start();
		}

		// Append l1 index when paging.
		l1WriteIndexManager.append(l1IndexKey, new L1IndexValue(l2IndexValue.getSequence()));

		// Flush l2 index before paging.
		if (l2WriteIndexManager != null) {
			l2WriteIndexManager.flush();
		}

		l2WriteIndexManager = indexManagerFinder.findNextL2WriteIndexManager();
		l2WriteIndexManager.start();
		l2WriteIndexManager.append(new L2IndexKey(l1IndexKey.getBinlogInfo()), l2IndexValue);
	}
}
