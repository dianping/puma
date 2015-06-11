package com.dianping.puma.core.model;

import java.io.Serializable;
import java.util.Date;

public class ServerAck implements Serializable{
	
	private static final long serialVersionUID = 5195049180415722540L;

	private String clientName;
	
	private String clientIp;
	
	private String taskName;
	
	private String serverName;
	
	private String serverIp;
	
	private BinlogInfo parserBinlog;
	
	private BinlogInfo senderBinlog;
	
	private Date updateTime;

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

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setParserBinlog(BinlogInfo parserBinlog) {
		this.parserBinlog = parserBinlog;
	}

	public BinlogInfo getParserBinlog() {
		return parserBinlog;
	}

	public void setSenderBinlog(BinlogInfo senderBinlog) {
		this.senderBinlog = senderBinlog;
	}

	public BinlogInfo getSenderBinlog() {
		return senderBinlog;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
