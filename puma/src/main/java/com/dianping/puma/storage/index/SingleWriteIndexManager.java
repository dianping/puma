package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.WriteBucket;
import com.dianping.puma.storage.index.WriteIndexManager;

import java.io.IOException;

public abstract class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {

	private final String filename;

	private final int bufSizeByte;

	private final int maxSizeByte;

	private WriteBucket writeBucket;

	public SingleWriteIndexManager(String filename, int bufSizeByte, int maxSizeByte) {
		this.filename = filename;
		this.bufSizeByte = bufSizeByte;
		this.maxSizeByte = maxSizeByte;
	}

	@Override
	protected void doStart() {
		writeBucket = BucketFactory.newLineWriteBucket(filename, bufSizeByte, maxSizeByte);
		writeBucket.start();
	}

	@Override
	protected void doStop() {
		if (writeBucket != null) {
			writeBucket.stop();
		}
	}

	@Override
	public void append(K indexKey, V indexValue) throws IOException {
		checkStop();

		byte[] data = encode(indexKey, indexValue);
		writeBucket.append(data);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		writeBucket.flush();
	}

	abstract protected byte[] encode(K indexKey, V indexValue);
}
