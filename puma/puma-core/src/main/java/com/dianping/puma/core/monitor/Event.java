package com.dianping.puma.core.monitor;

import java.util.Date;

public abstract class Event {

    protected Date createTime;

    protected String syncServerName;

    public Event() {
        this.createTime = new Date();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getSyncServerName() {
        return syncServerName;
    }

    public void setSyncServerName(String syncServerName) {
        this.syncServerName = syncServerName;
    }

}
