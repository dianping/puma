package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.Sequence;

import java.io.File;
import java.io.IOException;

public interface DataBucketManager extends LifeCycle {

	public File rootDir();

	public ReadDataBucket findReadDataBucket(Sequence sequence) throws IOException;

	public ReadDataBucket findNextReadDataBucket(Sequence sequence) throws IOException;

	public WriteDataBucket genNextWriteDataBucket() throws IOException;
}
