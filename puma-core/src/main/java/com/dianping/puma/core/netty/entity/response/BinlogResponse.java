package com.dianping.puma.core.netty.entity.response;

public abstract class BinlogResponse {

	private String clientName;

	private String token;

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
