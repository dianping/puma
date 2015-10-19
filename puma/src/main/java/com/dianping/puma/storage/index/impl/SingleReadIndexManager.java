package com.dianping.puma.storage.index.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.LocalFileReadBucket;
import com.dianping.puma.storage.bucket.ReadBucket;
import com.dianping.puma.storage.codec.Codec;
import com.dianping.puma.storage.codec.IndexCodec;
import com.dianping.puma.storage.index.IndexKey;
import com.dianping.puma.storage.index.IndexValue;
import com.dianping.puma.storage.index.ReadIndexManager;
import org.apache.commons.lang3.tuple.Pair;

import java.io.EOFException;
import java.io.IOException;

public class SingleReadIndexManager<K extends IndexKey<K>, V extends IndexValue> extends AbstractLifeCycle
		implements ReadIndexManager<K, V> {

	private String filename;

	private ReadBucket readBucket;

	private Codec<K, V> codec;

	public SingleReadIndexManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		readBucket = new LocalFileReadBucket(filename);
		readBucket.start();

		codec = new IndexCodec<K, V>();
		codec.start();
	}

	@Override
	protected void doStop() {
		readBucket.stop();
		codec.stop();
	}

	@Override
	public V findOldest() throws IOException {
		byte[] data = readBucket.next();
		if (data == null) {
			return null;
		}
		return codec.decode(data).getRight();
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
			return codec.decode(data).getRight();
		}
	}

	@Override
	public V find(K indexKey) throws IOException {
		byte[] data;
		try {
			while (true) {
				data = readBucket.next();
				if (data != null) {
					Pair<K, V> pair = codec.decode(data);
					if (indexKey.compareTo(pair.getLeft()) >= 0) {
						return pair.getRight();
					}
				}
			}
		} catch (EOFException eof) {
			return null;
		}
	}
}
