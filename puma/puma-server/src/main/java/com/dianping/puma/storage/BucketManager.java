package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;

public interface BucketManager extends LifeCycle<StorageLifeCycleException> {
	public Bucket getReadBucket(long seq) throws StorageClosedException, IOException;

	public Bucket getNextReadBucket(long seq) throws StorageClosedException, IOException;

	public Bucket getNextWriteBucket() throws StorageClosedException, IOException;

	public boolean hasNexReadBucket(long seq) throws StorageClosedException, IOException;

	public void updateLatestSequence(Sequence sequence);

}
