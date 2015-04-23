package com.dianping.puma.core.monitor.event;

import com.dianping.puma.core.constant.SyncType;

public class ShardSyncTaskControllerEvent extends TaskControllerEvent {
    private SyncType syncType;

    public SyncType getSyncType() {
        return syncType;
    }

    public void setSyncType(SyncType syncType) {
        this.syncType = syncType;
    }
}
