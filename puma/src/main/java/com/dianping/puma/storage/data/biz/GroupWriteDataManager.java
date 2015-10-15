package com.dianping.puma.storage.data.biz;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.data.manage.WriteDataManager;

import java.io.IOException;

public class GroupWriteDataManager extends AbstractLifeCycle implements WriteDataManager<DataKey, DataValue> {

	private String database;

	private String masterBaseDir = "/data/appdatas/puma/storage/master/";

	private String slaveBaseDir = "/data/appdatas/puma/storage/slave/";

	public GroupWriteDataManager(String database) {
		this.database = database;
	}

	public GroupWriteDataManager(String database, String masterBaseDir, String slaveBaseDir) {
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
	public int append(DataKey dataKey, DataValue dataValue) throws IOException {
		return 0;
	}

	@Override
	public void flush() throws IOException {

	}
}
