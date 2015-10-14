package com.dianping.puma.storage.index.manage;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {
	@Override protected void doStart() {

	}

	@Override protected void doStop() {

	}

	@Override public int append(K indexKey, V indexValue) throws IOException {
		return 0;
	}

	@Override public void flush() throws IOException {

	}
}
