package com.dianping.puma.storage.data.manage;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public class SingleWriteDataManager<K, V> extends AbstractLifeCycle implements WriteDataManager<K, V> {

	@Override protected void doStart() {

	}

	@Override protected void doStop() {

	}

	@Override public int append(K dataKey, V dataValue) throws IOException {
		return 0;
	}

	@Override public void flush() throws IOException {

	}
}
