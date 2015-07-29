package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.google.common.base.Objects;

import java.util.Date;

public class PumaTaskStateEntity {

    private int id;

    private String taskName;

    private String serverName;

    private Date updateTime;

    private String detail;

    private Status status;

    private BinlogInfo binlogInfo = new BinlogInfo();

    private BinlogStat binlogStat = new BinlogStat();

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

    public BinlogStat getBinlogStat() {
        return binlogStat;
    }

    public void setBinlogStat(BinlogStat binlogStat) {
        this.binlogStat = binlogStat;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PumaTaskStateEntity taskState = (PumaTaskStateEntity) o;
        return Objects.equal(taskName, taskState.taskName) && Objects.equal(serverName, taskState.serverName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(taskName, serverName);
    }
}
