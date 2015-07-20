package com.dianping.puma.biz.entity;

import java.util.Date;

public class PumaServerEntity {

    private int id;

    private String name;

    private String host;

    private int port;

    private float loadBalance;

    private Date gmtUpdate;

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

    public Date getGmtUpdate() {
        return gmtUpdate;
    }

    public void setGmtUpdate(Date gmtUpdate) {
        this.gmtUpdate = gmtUpdate;
    }
}
