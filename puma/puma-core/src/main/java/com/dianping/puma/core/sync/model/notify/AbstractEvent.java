package com.dianping.puma.core.sync.model.notify;

import java.util.Date;

public abstract class AbstractEvent implements Event {

    protected Date createTime;

    public AbstractEvent() {
        this.setCreateTime(new Date());
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
