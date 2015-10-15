package com.dianping.puma.storage.index.manage;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {

	private String database;

	public SingleWriteIndexManager(String database) {
		this.database = database;
	}

	@Override protected void doStart() {

	}

	@Override protected void doStop() {

	}

	@Override public void append(K indexKey, V indexValue) throws IOException {
	}

	@Override public void flush() throws IOException {

	}
}
