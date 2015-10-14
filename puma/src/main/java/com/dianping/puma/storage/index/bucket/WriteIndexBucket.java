package com.dianping.puma.storage.index.bucket;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface WriteIndexBucket extends LifeCycle {

	void append(byte[] data) throws IOException;

	void flush() throws IOException;
}
