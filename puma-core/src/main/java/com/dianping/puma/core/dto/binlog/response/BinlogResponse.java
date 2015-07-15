package com.dianping.puma.core.dto.binlog.response;

public abstract class BinlogResponse {

    private String msg;

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
