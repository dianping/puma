package com.dianping.puma.core.sync.model.task;

public enum SyncTaskStatusAction {
    START("第一次运行"),
    PAUSE("暂停"),
    RESTART("重新运行");

    private final String desc;

    private SyncTaskStatusAction(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
