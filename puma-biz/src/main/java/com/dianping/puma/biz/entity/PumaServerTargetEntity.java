package com.dianping.puma.biz.entity;

import java.util.Date;
import java.util.List;

public class PumaServerTargetEntity {

	private int id;

	private Date beginTime;

	private String ServerName;

	private String TargetDb;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public String getServerName() {
		return ServerName;
	}

	public void setServerName(String serverName) {
		ServerName = serverName;
	}

	public String getTargetDb() {
		return TargetDb;
	}

	public void setTargetDb(String targetDb) {
		TargetDb = targetDb;
	}
}
