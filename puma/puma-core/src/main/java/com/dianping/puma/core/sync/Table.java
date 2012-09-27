package com.dianping.puma.core.sync;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private String from;
    private String to;
    private Boolean partOf;

    private List<Column> columns = new ArrayList<Column>();

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

    public Boolean getPartOf() {
        return partOf;
    }

    public void setPartOf(Boolean partOf) {
        this.partOf = partOf;
    }


    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    @Override
    public String toString() {
        return "Table [from=" + from + ", to=" + to + ", partOf=" + partOf + ", columns=" + columns + "]";
    }

}
