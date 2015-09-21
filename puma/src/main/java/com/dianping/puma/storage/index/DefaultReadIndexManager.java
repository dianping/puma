package com.dianping.puma.storage.index;

import java.io.IOException;

public class DefaultReadIndexManager<K extends IndexKey, V extends IndexValue<IndexKey>> implements ReadIndexManager<K, V> {

	@Override
	public V findFirst() throws IOException {
		return null;
	}

	@Override
	public V findLatest() throws IOException {
		return null;
	}

	@Override
	public V findByTime(K searchKey, boolean startWithCompleteTransaction) throws IOException {
		return null;
	}

	@Override
	public V findByBinlog(K searchKey, boolean startWithCompleteTransaction) throws IOException {
		return null;
	}
}
