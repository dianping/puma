package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.ReadDataManager;
import com.dianping.puma.storage.data.impl.GroupReadDataManager;
import com.dianping.puma.storage.data.impl.DataKeyImpl;
import com.dianping.puma.storage.data.impl.DataValueImpl;
import com.dianping.puma.storage.index.ReadIndexManager;
import com.dianping.puma.storage.index.impl.SeriesReadIndexManager;
import com.dianping.puma.storage.index.impl.L1IndexKey;
import com.dianping.puma.storage.index.impl.L2IndexValue;

import java.io.IOException;

public class DefaultReadChannel extends AbstractLifeCycle implements ReadChannel {

	private String database;

	private ReadIndexManager<L1IndexKey, L2IndexValue> readIndexManager;

	private ReadDataManager<DataKeyImpl, DataValueImpl> readDataManager;

	protected DefaultReadChannel(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		readIndexManager = new SeriesReadIndexManager(database);
		readIndexManager.start();

		readDataManager = new GroupReadDataManager(database);
		readDataManager.start();
	}

	@Override
	protected void doStop() {
		readIndexManager.stop();
		readDataManager.stop();
	}

	@Override
	public void openOldest() throws IOException {
		L2IndexValue l2IndexValue = readIndexManager.findOldest();
		if (l2IndexValue == null) {
			throw new IOException("failed to open oldest.");
		}
		Sequence sequence = l2IndexValue.getSequence();
		readDataManager.open(new DataKeyImpl(sequence));
	}

	@Override
	public void openLatest() throws IOException {
		L2IndexValue l2IndexValue = readIndexManager.findLatest();
		if (l2IndexValue == null) {
			throw new IOException("failed to open latest.");
		}
		Sequence sequence = l2IndexValue.getSequence();
		readDataManager.open(new DataKeyImpl(sequence));
	}

	@Override
	public void open(BinlogInfo binlogInfo) throws IOException {
		L2IndexValue l2IndexValue = readIndexManager.find(new L1IndexKey(binlogInfo));
		if (l2IndexValue == null) {
			throw new IOException("failed to open.");
		}
		Sequence sequence = l2IndexValue.getSequence();
		readDataManager.open(new DataKeyImpl(sequence));
	}

	@Override
	public ChangedEvent next() throws IOException {
		DataValueImpl dataValueImpl = readDataManager.next();
		if (dataValueImpl == null) {
			throw new IOException("failed to next.");
		}
		return dataValueImpl.getBinlogEvent();
	}
}
