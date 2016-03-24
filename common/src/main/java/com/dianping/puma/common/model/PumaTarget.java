package com.dianping.puma.common.model;

import java.util.Date;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class PumaTarget {

    private int id;

    private String database;

    private String table;

    private Date beginTime;

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
