package com.dianping.puma.core.sync.model.action;

import java.util.Date;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.google.code.morphia.annotations.Id;

public abstract class ActionState {

    @Id
    private ObjectId id;//与Action一致
    //    当前状态
    private State state;
    //    创建时间
    private Date createTime;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
