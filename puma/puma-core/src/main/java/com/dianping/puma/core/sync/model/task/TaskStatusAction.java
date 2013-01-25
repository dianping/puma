package com.dianping.puma.core.sync.model.task;

public enum TaskStatusAction {
    START("运行"),
    PAUSE("暂停"),
    RESTART("重新运行");

    private final String desc;

    private TaskStatusAction(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
