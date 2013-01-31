package com.dianping.puma.core.monitor;

public class SyncTaskDeleteEvent extends Event {

    private long syncTaskId;

    public long getSyncTaskId() {
        return syncTaskId;
    }

    public void setSyncTaskId(long syncTaskId) {
        this.syncTaskId = syncTaskId;
    }

    @Override
    public String toString() {
        return "SyncTaskDeleteEvent [syncTaskId=" + syncTaskId + "]";
    }

}
