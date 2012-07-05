package com.dianping.puma.storage;

import java.io.IOException;

public interface BucketManager {
	public Bucket getBucket(int startFileNo) throws IOException;
}
