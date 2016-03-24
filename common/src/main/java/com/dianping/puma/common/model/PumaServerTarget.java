package com.dianping.puma.common.model;

import java.util.Date;
import java.util.List;

public class PumaServerTarget {

	private int id;

	private Date beginTime;

	private String ServerName;

	private String serverHost;

	private String TargetDb;

	private List<String> tables;

	private boolean stopped;

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

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public String getTargetDb() {
		return TargetDb;
	}

	public void setTargetDb(String targetDb) {
		TargetDb = targetDb;
	}

	public List<String> getTables() {
		return tables;
	}

	public void setTables(List<String> tables) {
		this.tables = tables;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
}
