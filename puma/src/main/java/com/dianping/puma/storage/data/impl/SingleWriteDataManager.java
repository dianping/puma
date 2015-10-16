package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.codec.Codec;
import com.dianping.puma.storage.codec.DataCodec;
import com.dianping.puma.storage.data.bucket.LocalFileWriteDataBucket;
import com.dianping.puma.storage.data.bucket.WriteDataBucket;
import com.dianping.puma.storage.data.WriteDataManager;

import java.io.IOException;

public class SingleWriteDataManager<K extends Comparable<K>, V> extends AbstractLifeCycle implements
		WriteDataManager<K, V> {

	private final long MAX_SIZE_BYTE = 1024L * 1024L * 1024L;

	private String filename;

	private WriteDataBucket writeDataBucket;

	private Codec<K, V> codec;

	private long offset;

	public SingleWriteDataManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		writeDataBucket = new LocalFileWriteDataBucket(filename);
		writeDataBucket.start();

		codec = new DataCodec<K, V>();
		codec.start();
	}

	@Override
	protected void doStop() {
		writeDataBucket.stop();
		codec.stop();
	}

	@Override
	public void append(K dataKey, V dataValue) throws IOException {
		byte[] data = codec.encode(dataKey, dataValue);
		writeDataBucket.append(data);
		offset += data.length;
	}

	@Override
	public void flush() throws IOException {
		writeDataBucket.flush();
	}

	@Override
	public boolean hasRemainingForWrite() {
		return offset < MAX_SIZE_BYTE;
	}
}
