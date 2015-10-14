package com.dianping.puma.storage.data.factory;

import com.dianping.puma.storage.data.manage.DataBucketManager;
import com.dianping.puma.storage.data.manage.LocalFileDataBucketManager;

public class DataBucketManagerFactory {

	public static DataBucketManager newDataBucketManager(String baseDir, String database) {
		return new LocalFileDataBucketManager(baseDir, database);
	}
}
