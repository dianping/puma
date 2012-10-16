package com.dianping.puma.core.sync;

import java.util.ArrayList;
import java.util.List;

public class TableConfig {
    private String from;
    private String to;
    private Boolean partOf;

    private List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

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


    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        this.columns.add(column);
    }

    @Override
    public String toString() {
        return "Table [from=" + from + ", to=" + to + ", partOf=" + partOf + ", columns=" + columns + "]";
    }

}
