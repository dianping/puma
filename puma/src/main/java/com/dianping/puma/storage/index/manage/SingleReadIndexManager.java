package com.dianping.puma.storage.index.manage;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.index.bucket.LocalFileReadIndexBucket;
import com.dianping.puma.storage.index.bucket.ReadIndexBucket;
import com.dianping.puma.storage.index.codec.DefaultIndexCodec;
import com.dianping.puma.storage.index.codec.IndexCodec;
import org.apache.commons.lang3.tuple.Pair;

import java.io.EOFException;
import java.io.IOException;

public class SingleReadIndexManager<K extends Comparable<K>, V> extends AbstractLifeCycle implements ReadIndexManager<K, V> {

	private String filename;

	private ReadIndexBucket readIndexBucket;

	private IndexCodec<K, V> indexCodec;

	public SingleReadIndexManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		readIndexBucket = new LocalFileReadIndexBucket(filename);
		readIndexBucket.start();

		indexCodec = new DefaultIndexCodec<K, V>();
		indexCodec.start();
	}

	@Override
	protected void doStop() {
		readIndexBucket.stop();
		indexCodec.stop();
	}

	@Override
	public V findOldest() throws IOException {
		byte[] data = readIndexBucket.next();
		if (data == null) {
			return null;
		}
		return indexCodec.decode(data).getRight();
	}

	@Override
	public V findLatest() throws IOException {
		byte[] data = null;
		try {
			while (true) {
				data = readIndexBucket.next();
			}
		} catch (EOFException eof) {
			if (data == null) {
				return null;
			}
			return indexCodec.decode(data).getRight();
		}
	}

	@Override
	public V find(K indexKey) throws IOException {
		byte[] data;
		try {
			while (true) {
				data = readIndexBucket.next();
				if (data != null) {
					Pair<K, V> pair = indexCodec.decode(data);
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
