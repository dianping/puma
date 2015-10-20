package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.WriteBucket;
import com.dianping.puma.storage.bucket.LocalFileWriteBucket;
import com.dianping.puma.storage.data.DataKey;
import com.dianping.puma.storage.data.DataValue;
import com.dianping.puma.storage.data.WriteDataManager;

import java.io.IOException;

public abstract class SingleWriteDataManager<K extends DataKey, V extends DataValue>
		extends AbstractLifeCycle implements WriteDataManager<K, V> {

	private final long MAX_SIZE_BYTE = 1024L * 1024L * 1024L;

	private String filename;

	private WriteBucket writeBucket;

	private long offset;

	public SingleWriteDataManager(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		writeBucket = new LocalFileWriteBucket(filename);
		writeBucket.start();
	}

	@Override
	protected void doStop() {
		writeBucket.stop();
	}

	@Override
	public void append(K dataKey, V dataValue) throws IOException {
		byte[] data = encode(dataKey, dataValue);
		writeBucket.append(data);
		offset += data.length;
	}

	@Override
	public void flush() throws IOException {
		writeBucket.flush();
	}

	@Override
	public boolean hasRemainingForWrite() {
		return offset < MAX_SIZE_BYTE;
	}

	abstract protected byte[] encode(K dataKey, V dataValue);
}
