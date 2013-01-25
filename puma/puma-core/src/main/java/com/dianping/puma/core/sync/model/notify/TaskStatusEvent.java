package com.dianping.puma.core.sync.model.notify;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.TaskStatus;
import com.dianping.puma.core.sync.model.task.Type;

public class TaskStatusEvent extends AbstractEvent {

    private long taskId;

    private Type type;

    private TaskStatus taskStatus;

    //binlog信息
    private BinlogInfo binlogInfo;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    @Override
    public String toString() {
        return "TaskStatusNotify [taskId=" + taskId + ", type=" + type + ", taskStatus=" + taskStatus + ", binlogInfo="
                + binlogInfo + "]";
    }

}
