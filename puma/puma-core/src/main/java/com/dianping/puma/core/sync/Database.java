package com.dianping.puma.core.sync;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private String from;
    private String to;

    private List<Table> tables = new ArrayList<Table>();

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public void addTable(Table table) {
        this.tables.add(table);
    }

    @Override
    public String toString() {
        return "Database [from=" + from + ", to=" + to + ", tables=" + tables + "]";
    }

}
