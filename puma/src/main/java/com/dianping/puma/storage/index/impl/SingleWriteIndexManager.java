package com.dianping.puma.storage.index.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.LocalFileWriteBucket;
import com.dianping.puma.storage.bucket.WriteBucket;
import com.dianping.puma.storage.codec.Codec;
import com.dianping.puma.storage.codec.IndexCodec;
import com.dianping.puma.storage.index.WriteIndexManager;

import java.io.IOException;

public class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {

	private String filename;

	private WriteBucket writeBucket;

	private Codec<K, V> codec;

	public SingleWriteIndexManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		writeBucket = new LocalFileWriteBucket(filename);
		writeBucket.start();

		codec = new IndexCodec<K, V>();
	}

	@Override
	protected void doStop() {
		writeBucket.stop();
		codec.start();
	}

	@Override
	public void append(K indexKey, V indexValue) throws IOException {
		byte[] data = codec.encode(indexKey, indexValue);
		writeBucket.append(data);
	}

	@Override
	public void flush() throws IOException {
		writeBucket.flush();
	}
}
