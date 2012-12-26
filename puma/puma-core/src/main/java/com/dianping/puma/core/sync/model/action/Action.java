package com.dianping.puma.core.sync.model.action;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Id;

/**
 * 代表一个动作，由puma-admin创建action(发派命令)，puma-syncServer获取action(执行命令)
 * 
 * @author wukezhu
 */
public abstract class Action {

    @Id
    private ObjectId id;
    private ActionType type;
    //    源：源数据库名称(如Dianping)
    private String srcMysqlName;
    //    目标：目标的数据库名称
    private String destMysqlName;
    //    目标：具体host列表的index
    private int indexOfDestMysqlHosts;
    //    指派执行者：sync-server的host
    private String syncServerHost;

    protected Action(ActionType type) {
        this.type = type;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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

    public int getIndexOfDestMysqlHosts() {
        return indexOfDestMysqlHosts;
    }

    public void setIndexOfDestMysqlHosts(int indexOfDestMysqlHosts) {
        this.indexOfDestMysqlHosts = indexOfDestMysqlHosts;
    }

    public String getSyncServerHost() {
        return syncServerHost;
    }

    public void setSyncServerHost(String syncServerHost) {
        this.syncServerHost = syncServerHost;
    }

    public enum ActionType {
        SYNC,
        DUMP,
        CATCHUP
    }
}
