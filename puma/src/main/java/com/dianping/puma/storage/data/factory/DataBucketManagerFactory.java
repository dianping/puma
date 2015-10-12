package com.dianping.puma.storage.data.factory;

import com.dianping.puma.storage.data.DataBucketManager;
import com.dianping.puma.storage.data.impl.LocalFileDataBucketManager;

public class DataBucketManagerFactory {

	public static DataBucketManager newDataBucketManager(String baseDir, String database) {
		return new LocalFileDataBucketManager(baseDir, database);
	}
}
