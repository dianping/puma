package com.dianping.puma.eventbus.event;

import com.dianping.puma.core.model.BinlogInfo;

/**
 * Dozer @ 15/8/27
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ClientPositionChangedEvent {
    private String clientName;

    private BinlogInfo binlogInfo;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }
}
