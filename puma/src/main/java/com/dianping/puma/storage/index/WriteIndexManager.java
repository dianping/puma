package com.dianping.puma.storage.index;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public interface WriteIndexManager<K, V> extends LifeCycle {

	void append(K indexKey, V indexValue) throws IOException;

	void flush() throws IOException;

	Sequence position();
}
