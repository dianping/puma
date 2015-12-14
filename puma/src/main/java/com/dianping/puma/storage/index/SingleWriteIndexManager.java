package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.WriteBucket;

import java.io.File;
import java.io.IOException;

public abstract class SingleWriteIndexManager<K, V> extends AbstractLifeCycle implements WriteIndexManager<K, V> {

	protected final File file;

	private final int bufSizeByte;

	private final int maxSizeByte;

	private String date;

	private int number;

	private WriteBucket writeBucket;

	public SingleWriteIndexManager(File file, int bufSizeByte, int maxSizeByte) {
		this.file = file;
		this.bufSizeByte = bufSizeByte;
		this.maxSizeByte = maxSizeByte;
	}

	public SingleWriteIndexManager(File file, String date, int number, int bufSizeByte, int maxSizeByte) {
		this.file = file;
		this.date = date;
		this.number = number;
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

		if (writeBucket != null) {
			writeBucket.flush();
		}
	}

	@Override
	public Sequence position() {
		checkStop();

		return new Sequence(date, number, writeBucket.position());
	}

	protected boolean hasRemainingForWrite(){
        checkStop();

        return writeBucket != null && writeBucket.hasRemainingForWrite();
	}

	abstract protected byte[] encode(K indexKey, V indexValue);
}
