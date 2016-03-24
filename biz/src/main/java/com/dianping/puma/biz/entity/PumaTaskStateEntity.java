package com.dianping.puma.biz.entity;

import com.dianping.puma.core.model.BinlogInfo;

import java.util.Date;

public class PumaTaskStateEntity extends BaseEntity {

    private String taskName;

    private String detail;

    private BinlogInfo binlogInfo = new BinlogInfo();

    public PumaTaskStateEntity() {
        updateTime = new Date();
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
}
