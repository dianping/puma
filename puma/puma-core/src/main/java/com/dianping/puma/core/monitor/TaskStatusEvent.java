package com.dianping.puma.core.monitor;

import java.util.List;

import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;

public class TaskStatusEvent extends Event {

    private List<TaskExecutorStatus> statusList;

    public List<TaskExecutorStatus> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<TaskExecutorStatus> statusList) {
        this.statusList = statusList;
    }

    @Override
    public String toString() {
        return "TaskStatusEvent [status=" + statusList + "]";
    }

    //    public static class Status {
    //        private long taskId;
    //
    //        private Type type;
    //
    //        private TaskStatus taskStatus;
    //
    //        //        private BinlogInfo binlogInfo;
    //
    //        private Date gmtCreate;
    //
    //        public Status() {
    //            gmtCreate = new Date();
    //        }
    //
    //        public Date getGmtCreate() {
    //            return gmtCreate;
    //        }
    //
    //        public long getTaskId() {
    //            return taskId;
    //        }
    //
    //        public void setTaskId(long taskId) {
    //            this.taskId = taskId;
    //        }
    //
    //        public Type getType() {
    //            return type;
    //        }
    //
    //        public void setType(Type type) {
    //            this.type = type;
    //        }
    //
    //        public TaskStatus getTaskStatus() {
    //            return taskStatus;
    //        }
    //
    //        public void setTaskStatus(TaskStatus taskStatus) {
    //            this.taskStatus = taskStatus;
    //        }

    //        public BinlogInfo getBinlogInfo() {
    //            return binlogInfo;
    //        }
    //
    //        public void setBinlogInfo(BinlogInfo binlogInfo) {
    //            this.binlogInfo = binlogInfo;
    //        }

    //    @Override
    //    public int hashCode() {
    //        final int prime = 31;
    //        int result = 1;
    //        result = prime * result + (int) (taskId ^ (taskId >>> 32));
    //        result = prime * result + ((type == null) ? 0 : type.hashCode());
    //        return result;
    //    }
    //
    //    public static int calHashCode(Type type, long taskId) {
    //        final int prime = 31;
    //        int result = 1;
    //        result = prime * result + (int) (taskId ^ (taskId >>> 32));
    //        result = prime * result + ((type == null) ? 0 : type.hashCode());
    //        return result;
    //    }

}
