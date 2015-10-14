package com.dianping.puma.storage.data.factory;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.bucket.ReadDataBucket;
import com.dianping.puma.storage.data.bucket.WriteDataBucket;
import com.dianping.puma.storage.data.bucket.LocalFileReadDataBucket;
import com.dianping.puma.storage.data.bucket.LocalFileWriteDataBucket;

import java.io.File;

public class DataBucketFactory {

	public static ReadDataBucket newLocalFileReadDataBucket(Sequence sequence, File file) {
		return new LocalFileReadDataBucket(sequence, file);
	}

	public static WriteDataBucket newLocalFileWriteDataBucket(Sequence sequence, File file) {
		return new LocalFileWriteDataBucket(sequence, file);
	}
}
