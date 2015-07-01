package com.dianping.puma.core.netty.entity;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;

import java.util.LinkedList;
import java.util.List;

public class BinlogMessage {

	private List<ChangedEvent> binlogEvents = new LinkedList<ChangedEvent>();

	public List<ChangedEvent> getBinlogEvents() {
		return binlogEvents;
	}

	public void setBinlogEvents(List<ChangedEvent> binlogEvents) {
		this.binlogEvents = binlogEvents;
	}

	public void addBinlogEvents(ChangedEvent binlogEvent) {
		binlogEvents.add(binlogEvent);
	}

	public BinlogInfo getLastBinlogInfo() {
		return null;
	}

	public int size() {
		return binlogEvents.size();
	}
}
