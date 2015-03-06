package com.dianping.puma.core.sync.model.task;

import java.util.Map;

import com.dianping.puma.core.sync.model.BaseEntity;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

/**
 * 代表一个Task，由puma-admin创建task(发派命令)，puma-syncServer获取task(执行命令)
 * 
 * @author wukezhu
 */
public abstract class Task extends BaseEntity {

    private static final long serialVersionUID = -2446587945497295737L;
    private Type type;
    
    private String srcDBInstanceId;
    //    源：源数据库名称(如Dianping)
    @Indexed(value = IndexDirection.ASC, name = "srcMysqlName", unique = false, dropDups = true)
    private String srcMysqlName;
    
    //  源：具体host
    private MysqlHost srcMysqlHost;
    //    目标：目标的数据库名称
    @Indexed(value = IndexDirection.ASC, name = "destMysqlName", unique = false, dropDups = true)
    private String destMysqlName;
    //    目标：具体host
    private MysqlHost destMysqlHost;
    //    指派执行者：sync-server的name
    @Indexed(value = IndexDirection.ASC, name = "syncServerName", unique = false, dropDups = true)
    private String syncServerName;
    // SyncTaskId
    private Long syncTaskId;
    //    //状态
    //    private TaskState taskState;
    //  源：BinlogInfo
    private BinlogInfo binlogInfo;
    /** errorCode对应的Handler */
    private Map<Integer, String> errorCodeHandlerNameMap;
    /** 默认handler */
    private String defaultHandler;

    private boolean executed;

    protected Task(Type type) {
        this.type = type;
        this.executed = false;
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    public Long getSyncTaskId() {
        return syncTaskId;
    }

    public void setSyncTaskId(Long syncTaskId) {
        this.syncTaskId = syncTaskId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSrcMysqlName() {
        return srcMysqlName;
    }

    public void setSrcDBInstanceId(String srcDBInstanceId) {
		this.srcDBInstanceId = srcDBInstanceId;
	}

	public String getSrcDBInstanceId() {
		return srcDBInstanceId;
	}

	public MysqlHost getSrcMysqlHost() {
        return srcMysqlHost;
    }

    public void setSrcMysqlHost(MysqlHost srcMysqlHost) {
        this.srcMysqlHost = srcMysqlHost;
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

    public Map<Integer, String> getErrorCodeHandlerNameMap() {
        return errorCodeHandlerNameMap;
    }

    public void setErrorCodeHandlerNameMap(Map<Integer, String> errorCodeHandlerNameMap) {
        this.errorCodeHandlerNameMap = errorCodeHandlerNameMap;
    }

    public String getDefaultHandler() {
   	return defaultHandler;
   }

	public void setDefaultHandler(String defaultHandler) {
   	this.defaultHandler = defaultHandler;
   }

    public boolean getExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

	@Override
   public String toString() {
	   return String
	         .format(
	               "Task [type=%s, srcMysqlName=%s, srcMysqlHost=%s, destMysqlName=%s, destMysqlHost=%s, syncServerName=%s, syncTaskId=%s, binlogInfo=%s, errorCodeHandlerNameMap=%s, defaultHandler=%s]",
	               type, srcMysqlName, srcMysqlHost, destMysqlName, destMysqlHost, syncServerName, syncTaskId,
	               binlogInfo, errorCodeHandlerNameMap, defaultHandler);
   }

    //    public TaskState getTaskState() {
    //        return taskState;
    //    }
    //
    //    public void setTaskState(TaskState taskState) {
    //        this.taskState = taskState;
    //    }

}
