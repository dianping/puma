package com.dianping.puma.storage.index.manage;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface WriteIndexManager<K, V> extends LifeCycle {

	int append(K indexKey, V indexValue) throws IOException;

	void flush() throws IOException;
}
