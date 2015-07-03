package com.dianping.puma.core.netty.entity;

import com.dianping.puma.core.model.BinlogInfo;

public class BinlogAck {

    private BinlogInfo binlogInfo;

    private String clientName;

    private String token;

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
