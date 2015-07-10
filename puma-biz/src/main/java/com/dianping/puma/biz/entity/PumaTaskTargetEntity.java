package com.dianping.puma.biz.entity;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class PumaTaskTargetEntity {

    private int id;

    private int taskId;

    private String database;

    private String tables;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
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
