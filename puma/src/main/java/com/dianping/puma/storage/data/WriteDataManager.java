package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public interface WriteDataManager<K, V> extends LifeCycle {

	K append(V dataValue) throws IOException;

	void flush() throws IOException;

	K position();

	boolean hasRemainingForWrite();
}
