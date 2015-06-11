package com.dianping.puma.core.model;

public class ClientInfo {
	
	private String clientName;
	
	private String clientIp;
	
	private BinlogInfo binlogInfo;

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

}
