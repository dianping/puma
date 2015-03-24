package com.dianping.puma.core.monitor.event;

import java.util.Date;

public abstract class Event {

    protected Date gmtCreate;

    protected String serverName;

    public Event() {
        this.gmtCreate = new Date();
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

}
