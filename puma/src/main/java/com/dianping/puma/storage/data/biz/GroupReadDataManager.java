package com.dianping.puma.storage.data.biz;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.data.manage.ReadDataManager;

import java.io.IOException;

public class GroupReadDataManager<K, V> extends AbstractLifeCycle implements ReadDataManager<K, V> {

	private String database;

	private String masterBaseDir = "/data/appdatas/puma/storage/master/";

	private String slaveBaseDir = "/data/appdatas/puma/storage/slave/";

	public GroupReadDataManager(String database) {
		this.database = database;
	}

	public GroupReadDataManager(String database, String masterBaseDir, String slaveBaseDir) {
		this.database = database;
		this.masterBaseDir = masterBaseDir;
		this.slaveBaseDir = slaveBaseDir;
	}

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public void open(K dataKey) throws IOException {

	}

	@Override
	public V next() throws IOException {
		return null;
	}
}
