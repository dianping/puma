package com.dianping.puma.storage.index.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.LengthWriteBucket;
import com.dianping.puma.storage.bucket.WriteBucket;
import com.dianping.puma.storage.index.WriteIndexManager;

import java.io.IOException;

public abstract class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {

	private String filename;

	private WriteBucket writeBucket;

	public SingleWriteIndexManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		writeBucket = BucketFactory.newLengthWriteBucket(filename);
		writeBucket.start();
	}

	@Override
	protected void doStop() {
		writeBucket.stop();
	}

	@Override
	public void append(K indexKey, V indexValue) throws IOException {
		byte[] data = encode(indexKey, indexValue);
		writeBucket.append(data);
	}

	@Override
	public void flush() throws IOException {
		writeBucket.flush();
	}

	abstract protected byte[] encode(K indexKey, V indexValue);
}
