package com.dianping.puma.storage.data.biz;

import com.dianping.puma.core.event.ChangedEvent;

public class DataValue {

	private ChangedEvent binlogEvent;

	public DataValue(ChangedEvent binlogEvent) {
		this.binlogEvent = binlogEvent;
	}

	public ChangedEvent getBinlogEvent() {
		return binlogEvent;
	}

	public void setBinlogEvent(ChangedEvent binlogEvent) {
		this.binlogEvent = binlogEvent;
	}
}
