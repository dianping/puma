package com.dianping.puma.core.model;

import java.util.Date;
import java.util.List;

public class ServerAck {
	
	private String serverName;
	
	private String serverIp;
	
	private List<PumaTaskDetail> taskDetails;
	
	private BinlogInfo parserBinlogInfo;
	
	private Date createDate;
	
	public void setParserBinlogInfo(BinlogInfo parserBinlogInfo) {
		this.parserBinlogInfo = parserBinlogInfo;
	}
	public BinlogInfo getParserBinlogInfo() {
		return parserBinlogInfo;
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
	public void setTaskDetails(List<PumaTaskDetail> taskDetails) {
		this.taskDetails = taskDetails;
	}
	public List<PumaTaskDetail> getTaskDetails() {
		return taskDetails;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getCreateDate() {
		return createDate;
	}

}
