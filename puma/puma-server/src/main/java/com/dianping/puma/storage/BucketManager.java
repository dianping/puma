package com.dianping.puma.storage;

import java.io.IOException;

public interface BucketManager {
	public Bucket getReadBucket(long seq) throws IOException;

	public Bucket getNextReadBucket(long seq) throws IOException;

	public Bucket getNextWriteBucket() throws IOException;

}
