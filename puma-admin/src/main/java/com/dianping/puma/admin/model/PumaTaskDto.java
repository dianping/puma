package com.dianping.puma.admin.model;

import java.util.List;

public class PumaTaskDto {

	private String srcDBInstanceName;

	private String pumaServerName;

	private String name;

	private String binlogFile;

	private Long binlogPosition;

	private int preservedDay;
	
	private List<DatabaseDto> databases;
	
	private boolean disabled;
	
	private boolean isShow;
	
	public PumaTaskDto(){
		
	}
	
	public String getSrcDBInstanceName() {
		return srcDBInstanceName;
	}

	public void setSrcDBInstanceName(String srcDBInstanceName) {
		this.srcDBInstanceName = srcDBInstanceName;
	}

	public String getPumaServerName() {
		return pumaServerName;
	}

	public void setPumaServerName(String pumaServerName) {
		this.pumaServerName = pumaServerName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBinlogFile() {
		return binlogFile;
	}

	public void setBinlogFile(String binlogFile) {
		this.binlogFile = binlogFile;
	}

	public Long getBinlogPosition() {
		return binlogPosition;
	}

	public void setBinlogPosition(Long binlogPosition) {
		this.binlogPosition = binlogPosition;
	}

	public int getPreservedDay() {
		return preservedDay;
	}

	public void setPreservedDay(int preservedDay) {
		this.preservedDay = preservedDay;
	}

	public List<DatabaseDto> getDatabases() {
		return databases;
	}

	public void setDatabases(List<DatabaseDto> databases) {
		this.databases = databases;
	}

	public boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void setIsShow(boolean isShow) {
		this.isShow = isShow;
	}

	public boolean getIsShow() {
		return isShow;
	}

}
