package com.dianping.puma.storage.index;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface ReadIndexManager<K extends Comparable<K>, V> extends LifeCycle {

	V findOldest() throws IOException;

	V findLatest() throws IOException;

	V find(K indexKey) throws IOException;
}
