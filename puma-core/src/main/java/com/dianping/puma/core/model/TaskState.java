package com.dianping.puma.core.model;

import java.util.Date;

public abstract class TaskState {

	private Date gmtCreate;

	private String detail;

	public TaskState() {
		gmtCreate = new Date();
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
}
