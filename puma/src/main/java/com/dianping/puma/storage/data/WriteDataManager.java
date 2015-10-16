package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface WriteDataManager<K, V> extends LifeCycle {

	void append(K dataKey, V dataValue) throws IOException;

	void flush() throws IOException;

	boolean hasRemainingForWrite();
}
