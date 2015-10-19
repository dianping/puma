package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.ReadBucket;
import com.dianping.puma.storage.codec.Codec;
import com.dianping.puma.storage.codec.DataCodec;
import com.dianping.puma.storage.bucket.LocalFileReadBucket;
import com.dianping.puma.storage.data.DataKey;
import com.dianping.puma.storage.data.DataValue;
import com.dianping.puma.storage.data.ReadDataManager;

import java.io.IOException;

public class SingleReadDataManager<K extends DataKey, V extends DataValue> extends AbstractLifeCycle
		implements ReadDataManager<K, V> {

	private String filename;

	private ReadBucket readBucket;

	private Codec<K, V> codec;

	private K position;

	public SingleReadDataManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		readBucket = new LocalFileReadBucket(filename);
		readBucket.start();

		codec = new DataCodec<K, V>();
		codec.start();
	}

	@Override
	protected void doStop() {
		readBucket.stop();
		codec.stop();
	}

	@Override
	public K position() {
		return position;
	}

	@Override
	public void open(K dataKey) throws IOException {
		try {
			@SuppressWarnings("unchecked")
			K position = (K) dataKey.getClass().newInstance();
			this.position = position;
		} catch (InstantiationException e) {
			throw new RuntimeException("failed to new a position.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("failed to new a position.", e);
		}

		long offset = dataKey.offset();
		readBucket.skip(offset);

		position.addOffset(offset);
	}

	@Override
	public V next() throws IOException {
		byte[] data = readBucket.next();
		if (data == null) {
			return null;
		}
		position.addOffset(data.length);
		return codec.decode(data).getRight();
	}
}
