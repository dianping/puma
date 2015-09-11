package com.dianping.puma.biz.entity;

public class SyncTaskSyncServerEntity {

	int id;

	int taskId;

	int syncServerId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getSyncServerId() {
		return syncServerId;
	}

	public void setSyncServerId(int syncServerId) {
		this.syncServerId = syncServerId;
	}
}
