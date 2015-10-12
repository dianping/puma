package com.dianping.puma.storage.data.factory;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.ReadDataBucket;
import com.dianping.puma.storage.data.WriteDataBucket;
import com.dianping.puma.storage.data.impl.LocalFileReadDataBucket;
import com.dianping.puma.storage.data.impl.LocalFileWriteDataBucket;

import java.io.File;

public class DataBucketFactory {

	public static ReadDataBucket newLocalFileReadDataBucket(Sequence sequence, File file) {
		return new LocalFileReadDataBucket(sequence, file);
	}

	public static WriteDataBucket newLocalFileWriteDataBucket(Sequence sequence, File file) {
		return new LocalFileWriteDataBucket(sequence, file);
	}
}
