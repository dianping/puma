package com.dianping.puma.instance;

import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.core.model.TableSet;

import java.util.List;

/**
 * Dozer @ 8/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class InstanceChangedEvent {

    private String clusterName;

    private List<SrcDbEntity> dbList;

    private TableSet tableSet;

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

    public TableSet getTableSet() {
        return tableSet;
    }

    public void setTableSet(TableSet tableSet) {
        this.tableSet = tableSet;
    }


}
