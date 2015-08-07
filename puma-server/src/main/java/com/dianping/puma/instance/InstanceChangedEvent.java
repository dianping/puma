package com.dianping.puma.instance;

import com.dianping.puma.biz.entity.SrcDbEntity;

import java.util.List;

/**
 * Dozer @ 8/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class InstanceChangedEvent {

    private String clusterName;

    private List<SrcDbEntity> dbList;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<SrcDbEntity> getDbList() {
        return dbList;
    }

    public void setDbList(List<SrcDbEntity> dbList) {
        this.dbList = dbList;
    }
}
