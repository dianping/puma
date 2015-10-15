package com.dianping.puma.storage.index.manage;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.index.bucket.LocalFileWriteIndexBucket;
import com.dianping.puma.storage.index.bucket.WriteIndexBucket;
import com.dianping.puma.storage.index.codec.DefaultIndexCodec;
import com.dianping.puma.storage.index.codec.IndexCodec;

import java.io.IOException;

public class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {

	private String filename;

	private WriteIndexBucket writeIndexBucket;

	private IndexCodec<K, V> indexCodec;

	public SingleWriteIndexManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		writeIndexBucket = new LocalFileWriteIndexBucket(filename);
		writeIndexBucket.start();

		indexCodec = new DefaultIndexCodec<K, V>();
		indexCodec.start();
	}

	@Override
	protected void doStop() {
		writeIndexBucket.stop();
		indexCodec.start();
	}

	@Override
	public void append(K indexKey, V indexValue) throws IOException {
		byte[] data = indexCodec.encode(indexKey, indexValue);
		writeIndexBucket.append(data);
	}

	@Override
	public void flush() throws IOException {
		writeIndexBucket.flush();
	}
}
