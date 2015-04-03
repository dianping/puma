package com.dianping.puma.core.model.state;

public class ShardDumpTaskState extends BaseSyncTaskState {
    public static final int PERCENT_MAX = 100;

    private volatile int percent;

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
}
