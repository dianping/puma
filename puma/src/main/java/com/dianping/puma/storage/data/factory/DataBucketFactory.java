package com.dianping.puma.storage.data.factory;

import com.dianping.puma.storage.data.ReadDataBucket;
import com.dianping.puma.storage.data.impl.LocalFileReadDataBucket;

import java.io.File;

public class DataBucketFactory {

	public static ReadDataBucket newLocalFileReadDataBucket(File file) {
		return new LocalFileReadDataBucket(file);
	}
}
