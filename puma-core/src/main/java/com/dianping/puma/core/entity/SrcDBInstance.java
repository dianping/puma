package com.dianping.puma.core.entity;

public class SrcDBInstance extends BaseEntity {

	private String name;

	private Integer serverId;

	private String host;

	private Integer port;

	private String username;

	private String password;

	private String metaHost;

	private Integer metaPort;

	private String metaUsername;

	private String metaPassword;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
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

	public Integer getMetaPort() {
		return metaPort;
	}

	public void setMetaPort(Integer metaPort) {
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
