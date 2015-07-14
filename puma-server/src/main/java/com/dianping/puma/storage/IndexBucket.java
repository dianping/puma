package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.storage.exception.StorageClosedException;

public interface IndexBucket<K, V> extends LifeCycle<IOException> {

	V next() throws StorageClosedException, IOException;

	void locate(K key, boolean fromNext) throws StorageClosedException, IOException;
	
	V find(K key) throws StorageClosedException, IOException;

}
