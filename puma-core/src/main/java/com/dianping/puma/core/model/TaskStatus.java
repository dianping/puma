package com.dianping.puma.core.model;

import java.util.Date;

public abstract class TaskStatus {

	private String taskId;

	private Date gmtCreate;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
}
