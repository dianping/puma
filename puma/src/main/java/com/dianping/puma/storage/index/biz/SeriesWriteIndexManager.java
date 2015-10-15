package com.dianping.puma.storage.index.biz;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.index.manage.WriteIndexManager;

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
		indexManagerFinder = new LocalFileIndexManagerFinder(database, l1IndexBaseDir, l2IndexBaseDir);
		indexManagerFinder.start();

		// Find old L1 index bucket.
		l1WriteIndexManager = indexManagerFinder.findL1WriteIndexManager();
		l1WriteIndexManager.start();

		// Create a new L2 index bucket.
		l2WriteIndexManager = indexManagerFinder.findNextL2WriteIndexManager();
		l2WriteIndexManager.start();
	}

	@Override
	protected void doStop() {
		l1WriteIndexManager.stop();
		l2WriteIndexManager.stop();
	}

	@Override
	public void append(L1IndexKey l1IndexKey, L2IndexValue l2IndexValue) throws IOException {
		if (l1IndexKey.isNewBucket()) {
			l1WriteIndexManager.append(l1IndexKey, new L1IndexValue(l2IndexValue.getSequence()));

			// Create a new l2 index bucket.
			l2WriteIndexManager = indexManagerFinder.findNextL2WriteIndexManager();
			l2WriteIndexManager.start();
		}

		l2WriteIndexManager.append(new L2IndexKey(l1IndexKey.getBinlogInfo()), l2IndexValue);
	}

	@Override
	public void flush() throws IOException {
		l1WriteIndexManager.flush();
		l2WriteIndexManager.flush();
	}
}
