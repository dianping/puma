package com.dianping.puma.core.entity;

public class PumaTaskConfig {

	private String name;

	private String dbInstanceName;

	private String pumaServerName;

	private BinlogInfo binlogInfo;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDbInstanceName() {
		return dbInstanceName;
	}

	public void setDbInstanceName(String dbInstanceName) {
		this.dbInstanceName = dbInstanceName;
	}

	public String getPumaServerName() {
		return pumaServerName;
	}

	public void setPumaServerName(String pumaServerName) {
		this.pumaServerName = pumaServerName;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
