package com.dianping.puma.storage.index;

import java.io.IOException;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.storage.exception.StorageClosedException;

public interface IndexBucket<K, V> extends LifeCycle<IOException> {

	void append(byte[] bytes) throws IOException;
	
	V next() throws StorageClosedException, IOException;

	void locate(K key, boolean inclusive) throws StorageClosedException, IOException;

	void truncate() throws IOException;
}
