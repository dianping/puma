package com.dianping.puma.storage.index.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.index.ReadIndexManager;
import com.dianping.puma.storage.index.model.L1IndexKey;
import com.dianping.puma.storage.index.model.L2IndexValue;

import java.io.IOException;

public class MultiLevelReadIndexManager<K, V> extends AbstractLifeCycle implements ReadIndexManager<K, V> {

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public void open() throws IOException {

	}

	@Override
	public V findOldest() throws IOException {
		return null;
	}

	@Override
	public V findLatest() throws IOException {
		return null;
	}

	@Override
	public V find(K indexKey) throws IOException {
		return null;
	}
}
