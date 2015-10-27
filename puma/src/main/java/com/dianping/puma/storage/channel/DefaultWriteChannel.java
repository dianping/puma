package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.GroupWriteDataManager;
import com.dianping.puma.storage.index.SeriesWriteIndexManager;
import com.google.common.util.concurrent.Uninterruptibles;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DefaultWriteChannel extends AbstractLifeCycle implements WriteChannel {

	private String database;

	private SeriesWriteIndexManager writeIndexManager;

	private GroupWriteDataManager writeDataManager;

	private Long currServerId;

	private Date currDate;

	private Thread thread;

	protected DefaultWriteChannel(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		writeIndexManager = new SeriesWriteIndexManager(database);
		writeIndexManager.start();

		writeDataManager = new GroupWriteDataManager(database);
		writeDataManager.start();

		thread = new Thread(new FlushTask());
		thread.setDaemon(true);
		thread.setName("flush-" + database);
		thread.start();
	}

	@Override
	protected void doStop() {
		if (writeIndexManager != null) {
			writeIndexManager.stop();
		}

		if (writeDataManager != null) {
			writeDataManager.stop();
		}

		if (thread != null) {
			thread.interrupt();
		}
	}

	@Override
	public void append(ChangedEvent binlogEvent) throws IOException {
		checkStop();

		BinlogInfo binlogInfo = binlogEvent.getBinlogInfo();

		if (needToPage(binlogInfo, writeDataManager)) {
			Sequence sequence = writeDataManager.pageAppend(binlogEvent);
			writeIndexManager.pageAppend(binlogInfo, sequence);
		}

		Sequence sequence = writeDataManager.append(binlogEvent);
		writeIndexManager.append(binlogInfo, sequence);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		writeIndexManager.flush();
		writeDataManager.flush();
	}

	protected boolean needToPage(BinlogInfo binlogInfo, GroupWriteDataManager writeDataManager) {
		return !writeDataManager.hasRemainingForWriteOnCurrentPage();
	}

	private class FlushTask implements Runnable {
		@Override
		public void run() {
			while (!isStopped()) {
				try {
					flush();
					Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
				} catch (IOException ignore) {
				}
			}
		}
	}
}
