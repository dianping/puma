package com.dianping.puma.biz.entity;

import com.dianping.puma.core.model.BinlogInfo;

import java.util.Date;

public class PumaTaskStateEntity {

    private int id;

    private String taskName;

    private Date updateTime;

    private String detail;

    private BinlogInfo binlogInfo = new BinlogInfo();

    public PumaTaskStateEntity() {
        updateTime = new Date();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
