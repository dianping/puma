package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.exception.StorageClosedException;
import com.dianping.puma.exception.StorageInitException;

public interface BucketManager {
	public Bucket getReadBucket(long seq) throws StorageClosedException, IOException;

	public Bucket getNextReadBucket(long seq) throws StorageClosedException, IOException;

	public Bucket getNextWriteBucket() throws StorageClosedException, IOException;

	public boolean hasNexReadBucket(long seq) throws StorageClosedException, IOException;

	public void close();

	public void init() throws StorageInitException;

}
