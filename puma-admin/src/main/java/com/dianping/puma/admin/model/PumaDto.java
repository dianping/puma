package com.dianping.puma.admin.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PumaDto {

	private String database;

	private List<String> tables;

	private List<Integer> serverIds;

	private List<String> servers;

	private Date beginTime;

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

	public List<Integer> getServerIds() {
		return serverIds;
	}

	public void setServerIds(List<Integer> serverIds) {
		this.serverIds = serverIds;
	}

	public List<String> getServers() {
		return servers;
	}

	public void setServers(List<String> servers) {
		this.servers = servers;
	}

	public void addServer(String server) {
		if (servers == null) {
			servers = new ArrayList<String>();
		}

		servers.add(server);
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
}
