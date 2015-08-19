package com.dianping.puma.biz.entity;

import java.util.Date;
import java.util.List;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class PumaTargetEntity {

    private int id;

    private String database;

    private String formatTables;

    private List<String> tables;

    private Date beginTime;

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

    public String getFormatTables() {
        return formatTables;
    }

    public void setFormatTables(String formatTables) {
        this.formatTables = formatTables;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }
}
