package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.core.datatype.Pair;

public interface BucketManager {
	public Bucket getBucket(long seq) throws IOException;

	public Bucket getNextBucket(long seq);
	
	public Pair<Bucket, Long> getNextBucket(long seq);

}
