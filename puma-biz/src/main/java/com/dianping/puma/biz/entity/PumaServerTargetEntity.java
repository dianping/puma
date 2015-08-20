package com.dianping.puma.biz.entity;

import java.util.Date;
import java.util.List;

public class PumaServerTargetEntity {

	private int id;

	private int serverId;

	private String host;

	private int targetId;

	private String database;

	private List<String> tables;

	private String formatTables;

	private Date beginTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public List<String> getTables() {
		return tables;
	}

	public void setTables(List<String> tables) {
		this.tables = tables;
	}

	public String getFormatTables() {
		return formatTables;
	}

	public void setFormatTables(String formatTables) {
		this.formatTables = formatTables;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
}
