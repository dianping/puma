package com.dianping.puma.core.replicate.model.task;

import com.dianping.puma.core.replicate.model.BinlogInfo;

public class TaskExecutorStatus {

	private String serverName;
	
	private String host;
	
	private int port;
	
	private long taskId;
	
	private String taskName;
	
	private BinlogInfo startBinlogInfo;
	
	private BinlogInfo currentBinlogInfo;
	
	private long insertCount;
	
	private long updateCount;
	
	private long deleteCount;
	
	private long ddlCount;
	
	private long dbServerId;
	
	private StatusExecutorType executorStatus;

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setStartBinlogInfo(BinlogInfo startBinlogInfo) {
		this.startBinlogInfo = startBinlogInfo;
	}

	public BinlogInfo getStartBinlogInfo() {
		return startBinlogInfo;
	}

	public void setCurrentBinlogInfo(BinlogInfo currentBinlogInfo) {
		this.currentBinlogInfo = currentBinlogInfo;
	}

	public BinlogInfo getCurrentBinlogInfo() {
		return currentBinlogInfo;
	}

	public void setInsertCount(long insertCount) {
		this.insertCount = insertCount;
	}

	public long getInsertCount() {
		return insertCount;
	}

	public void setUpdateCount(long updateCount) {
		this.updateCount = updateCount;
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public void setDeleteCount(long deleteCount) {
		this.deleteCount = deleteCount;
	}

	public long getDeleteCount() {
		return deleteCount;
	}

	public void setDdlCount(long ddlCount) {
		this.ddlCount = ddlCount;
	}

	public long getDdlCount() {
		return ddlCount;
	}

	public void setDbServerId(long dbServerId) {
		this.dbServerId = dbServerId;
	}

	public long getDbServerId() {
		return dbServerId;
	}

	public void setExecutorStatus(StatusExecutorType executorStatus) {
		this.executorStatus = executorStatus;
	}

	public StatusExecutorType getExecutorStatus() {
		return executorStatus;
	}
	
}
