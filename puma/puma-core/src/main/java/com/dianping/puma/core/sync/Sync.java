package com.dianping.puma.core.sync;

public class Sync {

    private Config from;
    private Config to;
    private Instance instance;

    public Config getFrom() {
        return from;
    }

    public void setFrom(Config from) {
        this.from = from;
    }

    public Config getTo() {
        return to;
    }

    public void setTo(Config to) {
        this.to = to;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    @Override
    public String toString() {
        return "Sync [from=" + from + ", to=" + to + ", instance=" + instance + "]";
    }

}
