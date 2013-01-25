package com.dianping.puma.core.sync.model.notify;

import com.dianping.puma.core.sync.model.task.TaskStatusAction;
import com.dianping.puma.core.sync.model.task.Type;

public class TaskStatusActionEvent extends AbstractEvent {

    private long taskId;

    private Type type;

    private TaskStatusAction taskStatusAction;

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

    public TaskStatusAction getTaskStatusAction() {
        return taskStatusAction;
    }

    public void setTaskStatusAction(TaskStatusAction taskStatusAction) {
        this.taskStatusAction = taskStatusAction;
    }

    @Override
    public String toString() {
        return "TaskStatusActionNotify [taskId=" + taskId + ", type=" + type + ", taskStatusAction=" + taskStatusAction + "]";
    }

}
