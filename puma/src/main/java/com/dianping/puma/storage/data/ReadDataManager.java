package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public interface ReadDataManager<K, V> extends LifeCycle {

	void open(K dataKey) throws IOException;

	V next() throws IOException;
}
