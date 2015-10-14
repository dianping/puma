package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.data.ReadDataManager;

import java.io.IOException;

public class GroupReadDataManager<K, V> extends AbstractLifeCycle implements ReadDataManager<K, V> {

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public void open(K dataKey) throws IOException {

	}

	@Override
	public V next() throws IOException {
		return null;
	}
}
