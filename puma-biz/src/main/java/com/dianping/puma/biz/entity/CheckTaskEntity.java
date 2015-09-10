package com.dianping.puma.biz.entity;

import java.util.Date;
import java.util.Map;

public class CheckTaskEntity {

	private int id;

	private Date initTime;

	private Date currTime;

	private Date nextTime;

	private boolean running;

	private String ownerHost;

	private boolean success;

	private String message;

	private Date updateTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getInitTime() {
		return initTime;
	}

	public void setInitTime(Date initTime) {
		this.initTime = initTime;
	}

	public Date getCurrTime() {
		return currTime;
	}

	public void setCurrTime(Date currTime) {
		this.currTime = currTime;
	}

	public Date getNextTime() {
		return nextTime;
	}

	public void setNextTime(Date nextTime) {
		this.nextTime = nextTime;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getOwnerHost() {
		return ownerHost;
	}

	public void setOwnerHost(String ownerHost) {
		this.ownerHost = ownerHost;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
