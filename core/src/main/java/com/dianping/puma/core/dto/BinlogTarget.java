package com.dianping.puma.core.dto;

public class BinlogTarget {

	private String targetName;

	private long dbServerId;

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public long getDbServerId() {
		return dbServerId;
	}

	public void setDbServerId(long dbServerId) {
		this.dbServerId = dbServerId;
	}
}
