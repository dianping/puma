package com.dianping.puma.biz.entity.old;

public class PumaServer extends BaseEntity {

	private String host;

	private Integer port;

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
}
