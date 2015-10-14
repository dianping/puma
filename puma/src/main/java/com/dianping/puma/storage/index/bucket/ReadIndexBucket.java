package com.dianping.puma.storage.index.bucket;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface ReadIndexBucket extends LifeCycle {

	byte[] next() throws IOException;

	void skip(long offset) throws IOException;
}
