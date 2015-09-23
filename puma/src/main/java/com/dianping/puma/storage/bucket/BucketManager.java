package com.dianping.puma.storage.bucket;

import java.io.IOException;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.DataBucket;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;

public interface BucketManager extends LifeCycle<StorageLifeCycleException> {
	public DataBucket getReadBucket(long seq, boolean fromNext) throws StorageClosedException, IOException;
	
	public DataBucket getNextReadBucket(long seq) throws StorageClosedException, IOException;

	public DataBucket getNextWriteBucket() throws StorageClosedException, IOException;

	public boolean hasNexReadBucket(long seq) throws StorageClosedException, IOException;

	public void updateLatestSequence(Sequence sequence);

}
