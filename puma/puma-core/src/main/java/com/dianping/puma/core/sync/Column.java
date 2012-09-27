package com.dianping.puma.core.sync;

public class Column {
    private String from;
    private String to;
    private Boolean primary;

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

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        return "Column [from=" + from + ", to=" + to + ", primary=" + primary + "]";
    }

}
