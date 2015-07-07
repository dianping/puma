package com.dianping.puma.biz.sync.model.task;


public class SyncTask extends AbstractTask {

    private static final long serialVersionUID = 2359517002901314187L;

    // 状态
    private SyncTaskStatusAction syncTaskStatusAction;

    public SyncTask() {
        super(Type.SYNC);
    }

    public SyncTaskStatusAction getSyncTaskStatusAction() {
        return syncTaskStatusAction;
    }

    public void setSyncTaskStatusAction(SyncTaskStatusAction taskStatusAction) {
        this.syncTaskStatusAction = taskStatusAction;
    }

}
