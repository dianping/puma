package com.dianping.puma.storage.index.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.codec.Codec;
import com.dianping.puma.storage.codec.IndexCodec;
import com.dianping.puma.storage.index.WriteIndexManager;
import com.dianping.puma.storage.index.bucket.LocalFileWriteIndexBucket;
import com.dianping.puma.storage.index.bucket.WriteIndexBucket;

import java.io.IOException;

public class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {

	private String filename;

	private WriteIndexBucket writeIndexBucket;

	private Codec<K, V> codec;

	public SingleWriteIndexManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		writeIndexBucket = new LocalFileWriteIndexBucket(filename);
		writeIndexBucket.start();

		codec = new IndexCodec<K, V>();
	}

	@Override
	protected void doStop() {
		writeIndexBucket.stop();
		codec.start();
	}

	@Override
	public void append(K indexKey, V indexValue) throws IOException {
		byte[] data = codec.encode(indexKey, indexValue);
		writeIndexBucket.append(data);
	}

	@Override
	public void flush() throws IOException {
		writeIndexBucket.flush();
	}
}
