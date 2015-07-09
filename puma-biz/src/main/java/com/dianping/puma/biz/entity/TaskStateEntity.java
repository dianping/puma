package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.google.common.base.Objects;

import java.util.Date;

public class TaskStateEntity {
    private int id;

    private String name;

    private String serverName;

    private String taskType;

    private Date gmtUpdate;

    private String taskName;

    private String detail;

    private Status status;

    private ActionController controller;

    private BinlogInfo binlogInfo = new BinlogInfo();

    private BinlogStat binlogStat = new BinlogStat();

    public TaskStateEntity() {
        gmtUpdate = new Date();
    }

    public Date getGmtUpdate() {
        return gmtUpdate;
    }

    public void setGmtUpdate(Date gmtUpdate) {
        this.gmtUpdate = gmtUpdate;
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

    public ActionController getController() {
        return controller;
    }

    public void setController(ActionController controller) {
        this.controller = controller;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
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
        TaskStateEntity taskState = (TaskStateEntity) o;
        return Objects.equal(name, taskState.name) && Objects.equal(serverName, taskState.serverName)
                && Objects.equal(taskType, taskState.taskType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, serverName, taskType);
    }
}
