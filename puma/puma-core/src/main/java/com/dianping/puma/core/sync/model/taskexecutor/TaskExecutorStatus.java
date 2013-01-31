package com.dianping.puma.core.sync.model.taskexecutor;

import java.util.Date;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.Type;

public class TaskExecutorStatus {

    public TaskExecutorStatus() {
        gmtCreate = new Date();
    }

    private long taskId;

    private Type type;

    private Status status;

    private BinlogInfo binlogInfo;
    //  详细detail信息
    private String detail;

    private Date gmtCreate;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (taskId ^ (taskId >>> 32));
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    public static int calHashCode(Type type, long taskId) {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (taskId ^ (taskId >>> 32));
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TaskExecutorStatus [taskId=" + taskId + ", type=" + type + ", status=" + status + ", binlogInfo=" + binlogInfo
                + ", detail=" + detail + ", gmtCreate=" + gmtCreate + "]";
    }

    public enum Status {
        WAITING("刚刚创建或加载，未有状态信息"),
        PREPARING("准备运行"),
        RUNNING("运行中"),
        DUMPING("Dump：Dumping操作进行中"),
        LOADING("Dump：Loading操作进行中"),
        SUSPPENDED("已暂停"),
        FAILED("结束-失败"),
        SUCCEED("结束-成功");

        private final String desc;

        private Status(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
