package com.dianping.puma.storage.index;

import java.io.IOException;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.storage.exception.StorageClosedException;

public interface IndexBucket<K, V> extends LifeCycle<IOException> {

	V next() throws StorageClosedException, IOException;

	void locate(K key) throws StorageClosedException, IOException;

//	V findById(K key) throws StorageClosedException, IOException;
}
