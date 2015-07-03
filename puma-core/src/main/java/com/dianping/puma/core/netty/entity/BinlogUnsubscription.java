package com.dianping.puma.core.netty.entity;

public class BinlogUnsubscription {

    private String clientName;

    private String token;

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
