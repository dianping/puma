package com.dianping.puma.core.dto;

import com.dianping.puma.core.event.EventWrap;
import com.dianping.puma.core.model.BinlogInfo;

import java.util.LinkedList;
import java.util.List;

public class BinlogMessage {

    private List<EventWrap> binlogEvents = new LinkedList<EventWrap>();

    public List<EventWrap> getBinlogEvents() {
        return binlogEvents;
    }

    public void setBinlogEvents(List<EventWrap> binlogEvents) {
        this.binlogEvents = binlogEvents;
    }

    public void addBinlogEvents(EventWrap binlogEvent) {
        binlogEvents.add(binlogEvent);
    }

    public BinlogInfo getLastBinlogInfo() {
        return null;
    }

    public int size() {
        return binlogEvents.size();
    }
}
