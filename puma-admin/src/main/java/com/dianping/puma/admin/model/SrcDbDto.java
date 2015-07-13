package com.dianping.puma.admin.model;

public class SrcDbDto {

	private String name;

	private String jdbcRef;

	private String host;

	private int port;

	private String username;

	private String password;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJdbcRef() {
		return jdbcRef;
	}

	public void setJdbcRef(String jdbcRef) {
		this.jdbcRef = jdbcRef;
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
}
