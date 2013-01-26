package com.dianping.puma.core.sync.model.notify;

import com.dianping.puma.core.sync.model.task.Type;

public class TaskEvent extends Event {

    private Type type;

    private long taskId;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

}
