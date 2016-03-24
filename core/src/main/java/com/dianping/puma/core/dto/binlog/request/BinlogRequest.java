package com.dianping.puma.core.dto.binlog.request;

import com.dianping.puma.core.dto.BinlogHttpMessage;

public abstract class BinlogRequest extends BinlogHttpMessage {

	private String clientName;

	private String token;

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
