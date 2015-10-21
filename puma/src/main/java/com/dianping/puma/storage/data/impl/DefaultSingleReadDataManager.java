package com.dianping.puma.storage.data.impl;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public final class DefaultSingleReadDataManager extends SingleReadDataManager<DataKeyImpl, DataValueImpl> {

	private EventCodec eventCodec = new RawEventCodec();

	public DefaultSingleReadDataManager(String filename) {
		super(filename);
	}

	@Override
	protected Pair<DataKeyImpl, DataValueImpl> decode(byte[] data) throws IOException {
		Event event = eventCodec.decode(data);
		if (!(event instanceof ChangedEvent)) {
			throw new IOException("unknown binlog event format.");
		}

		ChangedEvent binlogEvent = (ChangedEvent) event;
		return Pair.of(null, new DataValueImpl(binlogEvent));
	}
}
