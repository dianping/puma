package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.DataKeyImpl;
import com.dianping.puma.storage.data.GroupWriteDataManager;
import com.dianping.puma.storage.data.DataValueImpl;
import com.dianping.puma.storage.index.L1IndexKey;
import com.dianping.puma.storage.index.L2IndexValue;
import com.dianping.puma.storage.index.SeriesWriteIndexManager;

import java.io.IOException;

public class DefaultWriteChannel extends AbstractLifeCycle implements WriteChannel {

	private String database;

	private SeriesWriteIndexManager writeIndexManager;

	private GroupWriteDataManager writeDataManager;

	private Long currServerId;

	private String currDate;

	protected DefaultWriteChannel(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		writeIndexManager = new SeriesWriteIndexManager(database);
		writeIndexManager.start();

		writeDataManager = new GroupWriteDataManager(database);
		writeDataManager.start();
	}

	@Override
	protected void doStop() {
		if (writeIndexManager != null) {
			writeIndexManager.stop();
		}

		if (writeDataManager != null) {
			writeDataManager.stop();
		}
	}

	@Override
	public void append(BinlogInfo binlogInfo, ChangedEvent binlogEvent) throws IOException {
		Sequence sequence = nextSequence(binlogInfo);
		writeIndexManager.append(new L1IndexKey(binlogInfo), new L2IndexValue(sequence));
		writeDataManager.append(new DataKeyImpl(sequence), new DataValueImpl(binlogEvent));
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

	protected Sequence nextSequence(BinlogInfo binlogInfo) {
		return null;
	}

	private class FlushTask implements Runnable {
		@Override
		public void run() {
			try {
				flush();
			} catch (IOException ignore) {
			}
		}
	}
}
