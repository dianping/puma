package com.dianping.puma.storage.index;

import java.io.IOException;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.storage.exception.StorageClosedException;

public interface IndexBucket<K, V> extends LifeCycle<IOException> {

   // hack for test purpose
	void append(byte[] bytes) throws IOException;
	
	V next() throws StorageClosedException, IOException;
	
	void resetNext() throws IOException;

	void locate(K key, boolean inclusive) throws StorageClosedException, IOException;

	void truncate() throws IOException;

	K getStartKeyIndex();
}
