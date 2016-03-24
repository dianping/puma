package com.dianping.puma.biz.entity;

import java.util.Date;

public class PumaServerEntity extends BaseEntity {

    private static final long timeout = 60 * 1000; // 60s.

    private String name;

    private String host;

    private int port;

    private float loadBalance;

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

    public boolean checkAlive() {
        return new Date().getTime() - updateTime.getTime() < timeout;
    }
}
