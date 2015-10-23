package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.ReadBucket;
import com.dianping.puma.storage.index.IndexKey;
import com.dianping.puma.storage.index.IndexValue;
import com.dianping.puma.storage.index.ReadIndexManager;
import org.apache.commons.lang3.tuple.Pair;

import java.io.EOFException;
import java.io.IOException;

public abstract class SingleReadIndexManager<K extends IndexKey<K>, V extends IndexValue>
		extends AbstractLifeCycle implements ReadIndexManager<K, V> {

	private final String filename;

	private final int bufSizeByte;

	private final int avgSizeByte;

	private ReadBucket readBucket;

	public SingleReadIndexManager(String filename, int bufSizeByte, int avgSizeByte) {
		this.filename = filename;
		this.bufSizeByte = bufSizeByte;
		this.avgSizeByte = avgSizeByte;
	}

	@Override
	protected void doStart() {
		readBucket = BucketFactory.newLineReadBucket(filename, bufSizeByte, avgSizeByte);
		readBucket.start();
	}

	@Override
	protected void doStop() {
		if (readBucket != null) {
			readBucket.stop();
		}
	}

	@Override
	public V findOldest() throws IOException {
		checkStop();

		try {
			byte[] data = readBucket.next();
			return decode(data).getRight();
		} catch (EOFException eof) {
			return null;
		}
	}

	@Override
	public V findLatest() throws IOException {
		checkStop();

		byte[] data = null;
		try {
			while (true) {
				data = readBucket.next();
			}
		} catch (EOFException eof) {
			if (data == null) {
				return null;
			}
			return decode(data).getRight();
		}
	}

	@Override
	public V find(K indexKey) throws IOException {
		checkStop();

		byte[] data;
		try {
			while (true) {
				data = readBucket.next();
				if (data != null) {
					Pair<K, V> pair = decode(data);
					if (indexKey.compareTo(pair.getLeft()) >= 0) {
						return pair.getRight();
					}
				}
			}
		} catch (EOFException eof) {
			return null;
		}
	}

	abstract protected Pair<K, V> decode(byte[] data) throws IOException;
}
