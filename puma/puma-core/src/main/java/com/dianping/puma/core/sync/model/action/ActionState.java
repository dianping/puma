package com.dianping.puma.core.sync.model.action;

import java.util.Date;
import java.util.Map;

import com.dianping.puma.core.sync.model.BaseEntity;
import com.dianping.puma.core.sync.model.BinlogInfo;

public abstract class ActionState extends BaseEntity {

    private static final long serialVersionUID = 6392738680314560553L;
    //    当前状态
    private State state;
    //    创建时间
    private Date createTime;
    //  最后更新时间
    private Date lastUpdateTime;
    //    详细detail信息
    private String detail;
    //binlog信息
    private BinlogInfo binlogInfo;
    //自定义参数
    private Map<String, String> params;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
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

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public enum State {
        PREPARABLE("待准备"),
        PREPARING("正在准备执行"),
        RUNNABLE("待运行"),
        RUNNING("运行中"),
        PAUSE("待暂停"),
        SUSPPENDED("已暂停"),
        RESOLVED("已修复，待重新运行"),
        FAILED("结束-失败"),
        SUCCEED("结束-成功");

        private final String desc;

        private State(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
