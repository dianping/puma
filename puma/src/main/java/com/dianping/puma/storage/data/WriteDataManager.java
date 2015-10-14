package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface WriteDataManager<V> extends LifeCycle {

	int append(V dataValue) throws IOException;

	void flush() throws IOException;
}
