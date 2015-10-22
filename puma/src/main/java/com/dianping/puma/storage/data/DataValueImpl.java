package com.dianping.puma.storage.data;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.data.DataValue;

public final class DataValueImpl implements DataValue {

	private ChangedEvent binlogEvent;

	public DataValueImpl(ChangedEvent binlogEvent) {
		this.binlogEvent = binlogEvent;
	}

	public ChangedEvent getBinlogEvent() {
		return binlogEvent;
	}

	public void setBinlogEvent(ChangedEvent binlogEvent) {
		this.binlogEvent = binlogEvent;
	}
}
