package com.dianping.puma.storage.manage.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.manage.ReadDataManager;
import com.dianping.puma.storage.data.manage.GroupReadDataManager;
import com.dianping.puma.storage.data.model.DataKey;
import com.dianping.puma.storage.data.model.DataValue;
import com.dianping.puma.storage.index.manage.ReadIndexManager;
import com.dianping.puma.storage.index.manage.SeriesReadIndexManager;
import com.dianping.puma.storage.index.model.L1IndexKey;
import com.dianping.puma.storage.index.model.L2IndexValue;
import com.dianping.puma.storage.manage.ReadManager;

import java.io.IOException;

public class DefaultReadManager extends AbstractLifeCycle implements ReadManager {

	private String database;

	private ReadIndexManager<L1IndexKey, L2IndexValue> readIndexManager;

	private ReadDataManager<DataKey, DataValue> readDataManager;

	public DefaultReadManager(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		readIndexManager = new SeriesReadIndexManager<L1IndexKey, L2IndexValue>(database);
		readIndexManager.start();

		readDataManager = new GroupReadDataManager<DataKey, DataValue>(database);
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
		readDataManager.open(new DataKey(sequence));
	}

	@Override
	public void openLatest() throws IOException {
		L2IndexValue l2IndexValue = readIndexManager.findLatest();
		if (l2IndexValue == null) {
			throw new IOException("failed to open latest.");
		}
		Sequence sequence = l2IndexValue.getSequence();
		readDataManager.open(new DataKey(sequence));
	}

	@Override
	public void open(BinlogInfo binlogInfo) throws IOException {
		L2IndexValue l2IndexValue = readIndexManager.find(new L1IndexKey(binlogInfo));
		if (l2IndexValue == null) {
			throw new IOException("failed to open.");
		}
		Sequence sequence = l2IndexValue.getSequence();
		readDataManager.open(new DataKey(sequence));
	}

	@Override
	public ChangedEvent next() throws IOException {
		DataValue dataValue = readDataManager.next();
		if (dataValue == null) {
			throw new IOException("failed to next.");
		}
		return dataValue.getBinlogEvent();
	}
}
