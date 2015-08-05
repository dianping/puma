package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PumaTaskEntity {

    private int id;

    private String name;

    private int preservedDay;

    private String jdbcRef;

    private BinlogInfo startBinlogInfo;

    private Date updateTime;

    private TableSet tableSet;

    /**
     * Puma task instance may contain redundant information here.
     */
    private List<Pair<PumaServerEntity, ActionController>> pumaServers;

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

    public BinlogInfo getStartBinlogInfo() {
        return startBinlogInfo;
    }

    public void setStartBinlogInfo(BinlogInfo startBinlogInfo) {
        this.startBinlogInfo = startBinlogInfo;
    }

    public TableSet getTableSet() {
        return tableSet;
    }

    public void setTableSet(TableSet tableSet) {
        this.tableSet = tableSet;
    }

    public String getJdbcRef() {
        return jdbcRef;
    }

    public void setJdbcRef(String jdbcRef) {
        this.jdbcRef = jdbcRef;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<Pair<PumaServerEntity, ActionController>> getPumaServers() {
        return pumaServers;
    }

    public void setPumaServers(List<Pair<PumaServerEntity, ActionController>> pumaServers) {
        this.pumaServers = pumaServers;
    }

    /**
     * @return
     */
    public ActionController hostGetActionController(String host) {
        for (Pair<PumaServerEntity, ActionController> pumaServer : pumaServers) {
            if (pumaServer.getLeft().getHost().equals(host)) {
                return pumaServer.getRight();
            }
        }

        throw new RuntimeException("self get action controller failure.");
    }

    /**
     * @param actionController
     */
    public void hostSetActionController(String host, ActionController actionController) {
        for (Pair<PumaServerEntity, ActionController> pumaServer : pumaServers) {
            if (pumaServer.getLeft().getHost().equals(host)) {
                pumaServer.setValue(actionController);
                return;
            }
        }

        throw new RuntimeException("self set action controller failure.");
    }
}
