package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.codec.Codec;
import com.dianping.puma.storage.codec.DataCodec;
import com.dianping.puma.storage.data.bucket.LocalFileReadDataBucket;
import com.dianping.puma.storage.data.bucket.ReadDataBucket;
import com.dianping.puma.storage.data.DataKey;
import com.dianping.puma.storage.data.DataValue;
import com.dianping.puma.storage.data.ReadDataManager;

import java.io.IOException;

public class SingleReadDataManager<K extends DataKey, V extends DataValue> extends AbstractLifeCycle
		implements ReadDataManager<K, V> {

	private String filename;

	private ReadDataBucket readDataBucket;

	private Codec<K, V> codec;

	private K position;

	public SingleReadDataManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		readDataBucket = new LocalFileReadDataBucket(filename);
		readDataBucket.start();

		codec = new DataCodec<K, V>();
		codec.start();
	}

	@Override
	protected void doStop() {
		readDataBucket.stop();
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
		readDataBucket.skip(offset);

		position.addOffset(offset);
	}

	@Override
	public V next() throws IOException {
		byte[] data = readDataBucket.next();
		if (data == null) {
			return null;
		}
		position.addOffset(data.length);
		return codec.decode(data).getRight();
	}
}
