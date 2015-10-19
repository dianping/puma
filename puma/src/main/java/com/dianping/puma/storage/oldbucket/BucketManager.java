package com.dianping.puma.storage.oldbucket;

import java.io.IOException;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;

public interface BucketManager extends LifeCycle<StorageLifeCycleException> {
	public ReadDataBucket getReadBucket(long seq, boolean fromNext) throws StorageClosedException, IOException;
	
	public ReadDataBucket getNextReadBucket(long seq) throws StorageClosedException, IOException;

	public ReadDataBucket getNextWriteBucket() throws StorageClosedException, IOException;

	public boolean hasNexReadBucket(long seq) throws StorageClosedException, IOException;

	public void updateLatestSequence(Sequence sequence);

}
