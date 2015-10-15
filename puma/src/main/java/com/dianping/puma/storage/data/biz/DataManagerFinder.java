package com.dianping.puma.storage.data.biz;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.bucket.ReadDataBucket;
import com.dianping.puma.storage.data.bucket.WriteDataBucket;

import java.io.File;
import java.io.IOException;

public interface DataManagerFinder extends LifeCycle {

	public File rootDir();

	public ReadDataBucket findReadDataBucket(Sequence sequence) throws IOException;

	public ReadDataBucket findNextReadDataBucket(Sequence sequence) throws IOException;

	public WriteDataBucket genNextWriteDataBucket() throws IOException;
}
