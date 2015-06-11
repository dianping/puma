package com.dianping.puma.core.model;

import java.util.Date;

public class RelatedInfo {

	private String clientName;
	
	private String clientIp;
	
	private String taskName;
	
	private String serverName;
	
	private String serverIp;
	
	private BinlogInfo ackBinlog;
	
	private BinlogInfo parseBinlog;
	
	private BinlogInfo sendBinlog;
	
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

	public void setAckBinlog(BinlogInfo ackBinlog) {
		this.ackBinlog = ackBinlog;
	}

	public BinlogInfo getAckBinlog() {
		return ackBinlog;
	}

	public void setParseBinlog(BinlogInfo parseBinlog) {
		this.parseBinlog = parseBinlog;
	}

	public BinlogInfo getParseBinlog() {
		return parseBinlog;
	}

	public void setSendBinlog(BinlogInfo sendBinlog) {
		this.sendBinlog = sendBinlog;
	}

	public BinlogInfo getSendBinlog() {
		return sendBinlog;
	}
}
