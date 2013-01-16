package com.dianping.puma.core.sync.model.action;

import java.util.Date;

import com.dianping.puma.core.sync.model.BaseEntity;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

/**
 * 代表一个动作，由puma-admin创建action(发派命令)，puma-syncServer获取action(执行命令)
 * 
 * @author wukezhu
 */
public abstract class Action extends BaseEntity {

    private static final long serialVersionUID = -2446587945497295737L;
    private ActionType type;
    //    源：源数据库名称(如Dianping)
    @Indexed(value = IndexDirection.ASC, name = "srcMysqlName", unique = false, dropDups = true)
    private String srcMysqlName;
    //    目标：目标的数据库名称
    @Indexed(value = IndexDirection.ASC, name = "destMysqlName", unique = false, dropDups = true)
    private String destMysqlName;
    //    目标：具体host
    private MysqlHost destMysqlHost;
    //    指派执行者：sync-server的name
    @Indexed(value = IndexDirection.ASC, name = "syncServerName", unique = false, dropDups = true)
    private String syncServerName;
    //  创建时间
    private Date createTime;
    //  最后更新时间
    private Date lastUpdateTime;
    // SyncTaskId
    private Long syncTaskId;

    protected Action(ActionType type) {
        this.type = type;
    }

    public Long getSyncTaskId() {
        return syncTaskId;
    }

    public void setSyncTaskId(Long syncTaskId) {
        this.syncTaskId = syncTaskId;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getSrcMysqlName() {
        return srcMysqlName;
    }

    public void setSrcMysqlName(String srcMysqlName) {
        this.srcMysqlName = srcMysqlName;
    }

    public String getDestMysqlName() {
        return destMysqlName;
    }

    public void setDestMysqlName(String destMysqlName) {
        this.destMysqlName = destMysqlName;
    }

    public MysqlHost getDestMysqlHost() {
        return destMysqlHost;
    }

    public void setDestMysqlHost(MysqlHost destMysqlHost) {
        this.destMysqlHost = destMysqlHost;
    }

    public String getSyncServerName() {
        return syncServerName;
    }

    public void setSyncServerName(String syncServerName) {
        this.syncServerName = syncServerName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public enum ActionType {
        SYNC,
        DUMP,
        CATCHUP
    }
}
