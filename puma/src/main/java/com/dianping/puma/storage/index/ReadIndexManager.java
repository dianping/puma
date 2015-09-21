package com.dianping.puma.storage.index;

import java.io.IOException;

public interface ReadIndexManager<K, V> {

	public V findFirst() throws IOException;

	public V findLatest() throws IOException;

	public V findByTime(K searchKey, boolean startWithCompleteTransaction) throws IOException;

	public V findByBinlog(K searchKey, boolean startWithCompleteTransaction) throws IOException;
}
