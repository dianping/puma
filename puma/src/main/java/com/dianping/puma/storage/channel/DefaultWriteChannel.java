package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.impl.DataKeyImpl;
import com.dianping.puma.storage.data.impl.GroupWriteDataManager;
import com.dianping.puma.storage.data.impl.DataValueImpl;
import com.dianping.puma.storage.index.impl.L1IndexKey;
import com.dianping.puma.storage.index.impl.L2IndexValue;
import com.dianping.puma.storage.index.impl.SeriesWriteIndexManager;

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
		writeDataManager = new GroupWriteDataManager(database);
		writeDataManager.start();
	}

	@Override
	protected void doStop() {
		writeDataManager.stop();
	}

	@Override
	public void append(BinlogInfo binlogInfo, ChangedEvent binlogEvent) throws IOException {
		Sequence sequence = nextSequence(binlogInfo);
		writeIndexManager.append(new L1IndexKey(binlogInfo), new L2IndexValue(sequence));
		writeDataManager.append(new DataKeyImpl(sequence), new DataValueImpl(binlogEvent));
	}

	@Override
	public void flush() {

	}

	protected boolean needToPage(BinlogInfo binlogInfo, GroupWriteDataManager writeDataManager) {
		return false;
	}

	protected Sequence nextSequence(BinlogInfo binlogInfo) {
		return null;
	}

	private class FlushTask implements Runnable {
		@Override
		public void run() {
			flush();
		}
	}
}
