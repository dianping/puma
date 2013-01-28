package com.dianping.puma.core.sync.model.notify;

import java.util.List;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskStatus;

public class TaskStatusEvent extends Event {

    private List<Status> statusList;

    public List<Status> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<Status> statusList) {
        this.statusList = statusList;
    }

    @Override
    public String toString() {
        return "TaskStatusEvent [status=" + statusList + "]";
    }

    public static class Status {
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
            return "Status [taskId=" + taskId + ", type=" + type + ", taskStatus=" + taskStatus + ", binlogInfo=" + binlogInfo
                    + "]";
        }
    }
}
