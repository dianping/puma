package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.ReadBucket;
import com.dianping.puma.storage.data.ReadDataManager;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public final class SingleReadDataManager extends AbstractLifeCycle
		implements ReadDataManager<DataKeyImpl, DataValueImpl> {

	private final String filename;

	private final int bufSizeByte;

	private final int avgSizeByte;

	private ReadBucket readBucket;

	private EventCodec eventCodec = new RawEventCodec();

	private Sequence sequence;

	public SingleReadDataManager(String filename, int bufSizeByte, int avgSizeByte) {
		this.filename = filename;
		this.bufSizeByte = bufSizeByte;
		this.avgSizeByte = avgSizeByte;
	}

	@Override
	protected void doStart() {
		readBucket = BucketFactory.newLengthReadBucket(filename, bufSizeByte, avgSizeByte);
		readBucket.start();
	}

	@Override
	protected void doStop() {
		if (readBucket != null) {
			readBucket.stop();
		}
	}

	@Override
	public DataKeyImpl position() {
		checkStop();

		Sequence newSequence = new Sequence(sequence.getCreationDate(), sequence.getNumber(),
				(int) readBucket.position());
		return new DataKeyImpl(newSequence);
	}

	@Override
	public void open(DataKeyImpl dataKey) throws IOException {
		checkStop();

		sequence = new Sequence(dataKey.getSequence());
		long offset = sequence.getOffset();
		readBucket.skip(offset);
	}

	@Override
	public DataValueImpl next() throws IOException {
		checkStop();

		byte[] data = readBucket.next();
		if (data == null) {
			return null;
		}
		return decode(data).getRight();
	}

	protected Pair<DataKeyImpl, DataValueImpl> decode(byte[] data) throws IOException {
		Event event = eventCodec.decode(data);
		if (!(event instanceof ChangedEvent)) {
			throw new IOException("unknown binlog event format.");
		}

		ChangedEvent binlogEvent = (ChangedEvent) event;
		return Pair.of(null, new DataValueImpl(binlogEvent));
	}
}
