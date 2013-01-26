package com.dianping.puma.core.sync.model.notify;

import com.dianping.puma.core.sync.model.task.SyncTaskStatusAction;

public class SyncTaskStatusActionEvent extends Event {

    private long syncTaskId;

    private SyncTaskStatusAction taskStatusAction;

    public long getSyncTaskId() {
        return syncTaskId;
    }

    public void setSyncTaskId(long syncTaskId) {
        this.syncTaskId = syncTaskId;
    }

    public SyncTaskStatusAction getTaskStatusAction() {
        return taskStatusAction;
    }

    public void setTaskStatusAction(SyncTaskStatusAction taskStatusAction) {
        this.taskStatusAction = taskStatusAction;
    }

    @Override
    public String toString() {
        return "TaskStatusActionEvent [syncTaskId=" + syncTaskId + ", taskStatusAction=" + taskStatusAction + "]";
    }

}
