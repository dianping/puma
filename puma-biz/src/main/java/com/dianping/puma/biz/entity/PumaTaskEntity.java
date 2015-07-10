package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;

public class PumaTaskEntity {

    private int id;

    private String name;

    private BinlogInfo binlogInfo;

    private int preservedDay;

    private Status status;

    private PumaTaskDbEntity pumaTaskDb;

    private TableSet tableSet = new TableSet();

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

    public PumaTaskDbEntity getPumaTaskDb() {
        return pumaTaskDb;
    }

    public void setPumaTaskDb(PumaTaskDbEntity pumaTaskDb) {
        this.pumaTaskDb = pumaTaskDb;
    }
}
