package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.WriteBucket;

import java.io.IOException;

public final class SingleWriteDataManager extends AbstractLifeCycle
		implements WriteDataManager<DataKeyImpl, DataValueImpl> {

	private final String filename;

	private final int bufSizeByte;

	private final int maxSizeByte;

	private WriteBucket writeBucket;

	private EventCodec eventCodec = new RawEventCodec();

	protected SingleWriteDataManager(String filename, int bufSizeByte, int maxSizeByte) {
		this.filename = filename;
		this.bufSizeByte = bufSizeByte;
		this.maxSizeByte = maxSizeByte;
	}

	@Override
	protected void doStart() {
		writeBucket = BucketFactory.newLengthWriteBucket(filename, bufSizeByte, maxSizeByte);
		writeBucket.start();
	}

	@Override
	protected void doStop() {
		if (writeBucket != null) {
			writeBucket.stop();
		}
	}

	@Override
	public void append(DataKeyImpl dataKey, DataValueImpl dataValue) throws IOException {
		checkStop();

		byte[] data = encode(dataKey, dataValue);
		writeBucket.append(data);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		writeBucket.flush();
	}

	@Override
	public boolean hasRemainingForWrite() {
		checkStop();

		return writeBucket.hasRemainingForWrite();
	}

	protected byte[] encode(DataKeyImpl dataKey, DataValueImpl dataValue) throws IOException {
		ChangedEvent binlogEvent = dataValue.getBinlogEvent();
		return eventCodec.encode(binlogEvent);
	}
}
