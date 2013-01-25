package com.dianping.puma.core.sync.model.task;

public enum TaskStatus {
    PREPARING("正在准备运行"),
    RUNNING("运行中"),
    DUMPING("Dump：Dumping操作进行中"),
    LOADING("Dump：Loading操作进行中"),
    SUSPPENDED("已暂停"),
    FAILED("结束-失败"),
    SUCCEED("结束-成功");

    private final String desc;

    private TaskStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
