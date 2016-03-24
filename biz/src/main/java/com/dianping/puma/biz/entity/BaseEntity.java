package com.dianping.puma.biz.entity;

import java.util.Date;

/**
 * Created by xiaotian.li on 16/2/24.
 * Email: lixiaotian07@gmail.com
 */
public abstract class BaseEntity {

    protected int id;

    protected Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
