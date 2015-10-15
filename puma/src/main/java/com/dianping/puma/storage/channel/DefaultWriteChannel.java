package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.biz.DataKey;
import com.dianping.puma.storage.data.biz.GroupWriteDataManager;
import com.dianping.puma.storage.data.biz.DataValue;
import com.dianping.puma.storage.data.manage.WriteDataManager;
import com.dianping.puma.storage.index.biz.*;
import com.dianping.puma.storage.index.manage.WriteIndexManager;

import java.io.IOException;

public class DefaultWriteChannel extends AbstractLifeCycle implements WriteChannel {

	private String database;

	private String l1IndexBaseDir = "/data/appdatas/puma/binlogIndex/l1Index/";

	private String l2IndexBaseDir = "/data/appdatas/puma/binlogIndex/l2Index/";

	private WriteIndexManager<L1IndexKey, L2IndexValue> writeIndexManager;

	private WriteDataManager<DataKey, DataValue> writeDataManager;

	protected DefaultWriteChannel(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		writeIndexManager = new SeriesWriteIndexManager(database, l1IndexBaseDir, l2IndexBaseDir);
		writeIndexManager.start();

		writeDataManager = new GroupWriteDataManager(database);
		writeDataManager.start();
	}

	@Override
	protected void doStop() {
		writeIndexManager.stop();
		writeDataManager.stop();
	}

	@Override
	public void append(BinlogInfo binlogInfo, ChangedEvent binlogEvent) throws IOException {
		Sequence sequence = nextSequence(binlogInfo);
		writeIndexManager.append(new L1IndexKey(binlogInfo), new L2IndexValue(sequence));
		writeDataManager.append(new DataKey(sequence), new DataValue(binlogEvent));
	}

	@Override
	public void flush() {

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
