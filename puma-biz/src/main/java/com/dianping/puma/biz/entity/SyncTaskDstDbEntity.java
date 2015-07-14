package com.dianping.puma.biz.entity;

public class SyncTaskDstDbEntity {

	private int id;

	private int taskId;

	private int dstDbId;

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

	public int getDstDbId() {
		return dstDbId;
	}

	public void setDstDbId(int dstDbId) {
		this.dstDbId = dstDbId;
	}
}
