package com.dianping.puma.core.sync;

import java.util.ArrayList;
import java.util.List;

public class TableConfig {
    private String from;
    private String to;
    private boolean partOf = false;

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

    public boolean getPartOf() {
        return partOf;
    }

    public void setPartOf(boolean partOf) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + (partOf ? 1231 : 1237);
        result = prime * result + ((to == null) ? 0 : to.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TableConfig))
            return false;
        TableConfig other = (TableConfig) obj;
        if (columns == null) {
            if (other.columns != null)
                return false;
        } else if (!columns.equals(other.columns))
            return false;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from))
            return false;
        if (partOf != other.partOf)
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to))
            return false;
        return true;
    }

}
