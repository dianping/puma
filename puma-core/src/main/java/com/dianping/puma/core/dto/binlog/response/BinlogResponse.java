package com.dianping.puma.core.dto.binlog.response;

public abstract class BinlogResponse {

    private String clientName;

    private String token;

    private String msg;

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getClientName() {
        return clientName;
    }

    public String getToken() {
        return token;
    }

    public String getMsg() {
        return msg;
    }
}
