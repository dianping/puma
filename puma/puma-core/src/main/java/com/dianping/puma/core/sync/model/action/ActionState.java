package com.dianping.puma.core.sync.model.action;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.google.code.morphia.annotations.Id;

public abstract class ActionState {

    @Id
    private ObjectId id;//与Action一致
    //    当前状态
    private State state;
    //    操作的已耗时
    private long timeUsed;
    //    详细detail信息
    private String detail;
    //binlog信息
    private BinlogInfo binlogInfo;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    public enum State {
        CREATED,
        PREPARING,
        RUNNING,
        FAILED,
        SUCCEED
    }
}
