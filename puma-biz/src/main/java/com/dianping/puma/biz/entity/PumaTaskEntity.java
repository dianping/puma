package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;

import java.util.Date;
import java.util.List;

public class PumaTaskEntity {

    private int id;

    private String name;

    private BinlogInfo binlogInfo;

    private int preservedDay;

    private Status status;

    private TableSet tableSet;

    private List<SrcDbEntity> srcDbs;

    private List<PumaServerEntity> pumaServers;

    private Date UpdateTime;

    private ActionController actionController;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPreservedDay() {
        return preservedDay;
    }

    public void setPreservedDay(int preservedDay) {
        this.preservedDay = preservedDay;
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TableSet getTableSet() {
        return tableSet;
    }

    public void setSrcDbs(List<SrcDbEntity> srcDbs) {
        this.srcDbs = srcDbs;
    }

    public void setPumaServers(List<PumaServerEntity> pumaServers) {
        this.pumaServers = pumaServers;
    }

    public void setTableSet(TableSet tableSet) {
        this.tableSet = tableSet;
    }

    public List<SrcDbEntity> getSrcdbs() {
        return srcDbs;
    }

    public List<PumaServerEntity> getPumaServers() {
        return pumaServers;
    }

    public Date getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(Date updateTime) {
        UpdateTime = updateTime;
    }

    public ActionController getActionController() {
        return actionController;
    }

    public void setActionController(ActionController actionController) {
        this.actionController = actionController;
    }
}
