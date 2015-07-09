package com.dianping.puma.biz.entity;

public class SrcDbEntity {

	private int id;

	private String host;

	private int port;

	private String username;

	private String password;

	private long dbServerId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getDbServerId() {
		return dbServerId;
	}

	public void setDbServerId(long dbServerId) {
		this.dbServerId = dbServerId;
	}
}
