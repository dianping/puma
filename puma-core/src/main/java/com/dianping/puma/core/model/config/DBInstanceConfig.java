package com.dianping.puma.core.model.config;

import com.dianping.puma.core.model.BaseConfig;

public class DBInstanceConfig extends BaseConfig {

	private String name;

	private String host;

	private String port;

	private String username;

	private String password;

	private String metaHost;

	private String metaPort;

	private String metaUsername;

	private String metaPassword;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
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

	public String getMetaPort() {
		return metaPort;
	}

	public void setMetaPort(String metaPort) {
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
