package com.dianping.puma.biz.entity;

import com.dianping.puma.core.model.TableSet;
import com.google.common.collect.ImmutableSet;

import java.util.Date;
import java.util.Set;

public class PumaTaskEntity {

    private String name;

    private int preservedDay;

    private String clusterName;

    private Date updateTime;

    private TableSet tableSet;

    private Set<SrcDbEntity> srcDbEntityList = ImmutableSet.of();

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

    public Set<SrcDbEntity> getSrcDbEntityList() {
        return srcDbEntityList;
    }

    public void setSrcDbEntityList(Set<SrcDbEntity> srcDbEntityList) {
        this.srcDbEntityList = srcDbEntityList;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
