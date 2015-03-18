package com.dianping.puma.core.entity;

public class DBInstance extends BaseEntity {

	private Long serverId;

	private String host;

	private int port;

	private String username;

	private String password;

	private String metaHost;

	private int metaPort;

	private String metaUsername;

	private String metaPassword;

	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
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

	public String getMetaHost() {
		return metaHost;
	}

	public void setMetaHost(String metaHost) {
		this.metaHost = metaHost;
	}

	public int getMetaPort() {
		return metaPort;
	}

	public void setMetaPort(int metaPort) {
		this.metaPort = metaPort;
	}

	public String getMetaUsername() {
		return metaUsername;
	}

	public void setMetaUsername(String metaUsername) {
		this.metaUsername = metaUsername;
	}

	public String getMetaPassword() {
		return metaPassword;
	}

	public void setMetaPassword(String metaPassword) {
		this.metaPassword = metaPassword;
	}
}