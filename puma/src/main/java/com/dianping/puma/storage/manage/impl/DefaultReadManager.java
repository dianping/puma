package com.dianping.puma.storage.manage.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.data.DataBucketManager;
import com.dianping.puma.storage.data.ReadDataManager;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.index.*;
import com.dianping.puma.storage.manage.ReadManager;
import com.google.common.base.Strings;

import java.io.IOException;

public class DefaultReadManager extends AbstractLifeCycle implements ReadManager {

	private DataBucketManager dataBucketManager;

	private ReadDataManager readDataManager;

	private ReadIndexManager<IndexKeyImpl, IndexValueImpl> readIndexManager;

	@Override
	protected void doStart() {
		this.readIndexManager = new DefaultReadIndexManager<IndexKeyImpl, IndexValueImpl>(
				new IndexKeyConverter(), new IndexValueConverter());
	}

	@Override
	protected void doStop() {

	}

	@Override
	public void openByBinlog(BinlogInfo binlogInfo) throws IOException {

	}

	@Override
	public void openByTime(long timestamp) throws IOException {
		IndexValueImpl indexValue = readIndexManager.findByTime(new IndexKeyImpl(timestamp), true);
		if (indexValue == null) {
			throw new IOException("failed to find binlog in index.");
		}
		readDataManager.open(indexValue.getSequence());
	}

	@Override public void openFirst() throws IOException {

	}

	@Override public void openLast() throws IOException {

	}

	@Override public byte[] next() throws IOException {
		return new byte[0];
	}
}
