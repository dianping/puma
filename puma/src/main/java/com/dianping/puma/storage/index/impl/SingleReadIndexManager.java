package com.dianping.puma.storage.index.impl;

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

	private String filename;

	private ReadBucket readBucket;

	public SingleReadIndexManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		readBucket = BucketFactory.newLineReadBucket(filename);
		readBucket.start();
	}

	@Override
	protected void doStop() {
		readBucket.stop();
	}

	@Override
	public V findOldest() throws IOException {
		byte[] data = readBucket.next();
		if (data == null) {
			return null;
		}
		return decode(data).getRight();
	}

	@Override
	public V findLatest() throws IOException {
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

	abstract protected Pair<K, V> decode(byte[] data);
}
