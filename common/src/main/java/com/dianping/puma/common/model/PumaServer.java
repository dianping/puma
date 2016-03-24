package com.dianping.puma.common.model;

import java.util.Date;

public class PumaServer {

    private static final long timeout = 60 * 1000; // 60s.

    private int id;

    private String name;

    private String host;

    private int port;

    private float loadBalance;

    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public float getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(float loadBalance) {
        this.loadBalance = loadBalance;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public boolean checkAlive() {
        return new Date().getTime() - updateTime.getTime() < timeout;
    }
}
