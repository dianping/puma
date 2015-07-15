package com.dianping.puma.core.dto.binlog.response;

public class BinlogSubscriptionResponse extends BinlogResponse {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
