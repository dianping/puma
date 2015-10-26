package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.WriteBucket;
import com.dianping.puma.storage.index.WriteIndexManager;

import java.io.File;
import java.io.IOException;

public abstract class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {

	private final File file;

	private final int bufSizeByte;

	private final int maxSizeByte;

	private WriteBucket writeBucket;

	public SingleWriteIndexManager(File file, int bufSizeByte, int maxSizeByte) {
		this.file = file;
		this.bufSizeByte = bufSizeByte;
		this.maxSizeByte = maxSizeByte;
	}

	@Override
	protected void doStart() {
		writeBucket = BucketFactory.newLineWriteBucket(file, bufSizeByte, maxSizeByte);
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
