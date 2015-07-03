package com.dianping.puma.core.netty.entity;

public class BinlogSubscriptionResponse {
    private String token;

    public String getToken() {
        return token;
    }

    public BinlogSubscriptionResponse setToken(String token) {
        this.token = token;
        return this;
    }
}
