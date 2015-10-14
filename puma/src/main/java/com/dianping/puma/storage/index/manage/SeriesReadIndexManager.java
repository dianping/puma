package com.dianping.puma.storage.index.manage;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public class SeriesReadIndexManager<K, V> extends AbstractLifeCycle implements ReadIndexManager<K, V> {

	private String database;

	private String l1IndexBaseDir = "/data/appdatas/puma/binlogIndex/l1Index/";

	private String l2IndexBaseDir = "/data/appdatas/puma/binlogIndex/l2Index/";

	public SeriesReadIndexManager(String database) {
		this.database = database;
	}

	public SeriesReadIndexManager(String database, String l1IndexBaseDir, String l2IndexBaseDir) {
		this.database = database;
		this.l1IndexBaseDir = l1IndexBaseDir;
		this.l2IndexBaseDir = l2IndexBaseDir;
	}

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public V findOldest() throws IOException {
		return null;
	}

	@Override
	public V findLatest() throws IOException {
		return null;
	}

	@Override
	public V find(K indexKey) throws IOException {
		return null;
	}
}
