package com.dianping.puma.storage.index;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface WriteIndexManager<K, V> extends LifeCycle {

	void addL1Index(K key, String l2IndexName) throws IOException;

	void addL2Index(K key, V value) throws IOException;

	void removeByL2IndexName(String l2IndexName) throws IOException;

	void flush() throws IOException;

	IndexBucket<K, V> getIndexBucket(String fileName) throws IOException;

	boolean hasNextIndexBucket(String fileName) throws IOException;

	IndexBucket<K, V> getNextIndexBucket(String fileName) throws IOException;
}
