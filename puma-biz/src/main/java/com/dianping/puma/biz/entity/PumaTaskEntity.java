package com.dianping.puma.biz.entity;

import com.dianping.puma.core.model.TableSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PumaTaskEntity {

    private int id;

    private String name;

    private int preservedDay;

    private String clusterName;

    private Date updateTime;

    private TableSet tableSet;

    private List<SrcDbEntity> srcDbEntityList = new ArrayList<SrcDbEntity>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPreservedDay() {
        return preservedDay;
    }

    public void setPreservedDay(int preservedDay) {
        this.preservedDay = preservedDay;
    }

    public TableSet getTableSet() {
        return tableSet;
    }

    public void setTableSet(TableSet tableSet) {
        this.tableSet = tableSet;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<SrcDbEntity> getSrcDbEntityList() {
        return srcDbEntityList;
    }

    public void setSrcDbEntityList(List<SrcDbEntity> srcDbEntityList) {
        this.srcDbEntityList = srcDbEntityList;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
