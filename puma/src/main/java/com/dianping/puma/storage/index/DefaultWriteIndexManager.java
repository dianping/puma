package com.dianping.puma.storage.index;

import java.io.IOException;

public class DefaultWriteIndexManager<K extends IndexKey, V extends IndexValue<IndexKey>> implements WriteIndexManager<K, V> {

	@Override
	public void addL1Index(K key, String l2IndexName) throws IOException {

	}

	@Override
	public void addL2Index(K key, V value) throws IOException {

	}

	@Override
	public void removeByL2IndexName(String l2IndexName) throws IOException {

	}

	@Override
	public void flush() throws IOException {

	}

	@Override public IndexBucket<K, V> getIndexBucket(String fileName) throws IOException {
		return null;
	}

	@Override public boolean hasNextIndexBucket(String fileName) throws IOException {
		return false;
	}

	@Override public IndexBucket<K, V> getNextIndexBucket(String fileName) throws IOException {
		return null;
	}

	@Override public void start() {

	}

	@Override public void stop() {

	}
}
