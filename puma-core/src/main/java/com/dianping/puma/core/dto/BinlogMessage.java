package com.dianping.puma.core.dto;

import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.model.BinlogInfo;

import java.util.LinkedList;
import java.util.List;

public class BinlogMessage {

    private List<Event> binlogEvents = new LinkedList<Event>();

    public List<Event> getBinlogEvents() {
        return binlogEvents;
    }

    public void setBinlogEvents(List<Event> binlogEvents) {
        this.binlogEvents = binlogEvents;
    }

    public void addBinlogEvents(Event binlogEvent) {
        binlogEvents.add(binlogEvent);
    }

    public BinlogInfo getLastBinlogInfo() {
        if (binlogEvents.size() == 0) {
            return null;
        }
        return binlogEvents.get(binlogEvents.size() - 1).getBinlogInfo();
    }

    public int size() {
        return binlogEvents.size();
    }
}
