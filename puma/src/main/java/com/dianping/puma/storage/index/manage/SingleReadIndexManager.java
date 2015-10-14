package com.dianping.puma.storage.index.manage;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public class SingleReadIndexManager<K, V> extends AbstractLifeCycle implements ReadIndexManager<K, V> {

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

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
