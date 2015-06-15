package com.dianping.puma.core.monitor.event;

import java.util.Date;
import java.util.List;

public abstract class Event {

    protected Date gmtCreate;

    protected List<String> serverNames;

    public Event() {
        this.gmtCreate = new Date();
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public List<String> getServerNames() {
        return serverNames;
    }

    public void setServerNames(List<String> serverNames) {
        this.serverNames = serverNames;
    }

}
