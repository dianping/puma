package com.dianping.puma.biz.entity;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class PumaTaskTarget {

    private int id;

    private int pumaTaskId;

    private String database;

    private String tables;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPumaTaskId() {
        return pumaTaskId;
    }

    public void setPumaTaskId(int pumaTaskId) {
        this.pumaTaskId = pumaTaskId;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }
}
