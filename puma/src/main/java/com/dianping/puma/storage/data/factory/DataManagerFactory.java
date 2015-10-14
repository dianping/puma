package com.dianping.puma.storage.data.factory;

import com.dianping.puma.storage.data.ReadDataManager;
import com.dianping.puma.storage.data.impl.deprecated.DefaultReadDataManager;

public class DataManagerFactory {

	private static final String masterBaseDir = "/data/appdatas/puma/storage/master/";

	private static final String slaveBaseDir = "/data/appdatas/puma/storage/slave";

	public static ReadDataManager newReadDataManager(String database) {
		return new DefaultReadDataManager(masterBaseDir, slaveBaseDir, database);
	}
}
