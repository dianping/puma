package com.dianping.puma.admin.model;

public class SyncTaskDto {

	private String name;
	
	private String pumaTaskName;
	
	private String syncServerName;
	
	private String dstDBInstanceName;
	
	private MysqlMappingDto mysqlMapping;
	
	private String binlogFile;

	private Long binlogPosition;
	
	private boolean ddl;

	private boolean dml;

	private boolean transaction;
	
	private boolean consistent;
	
	private ErrorListDto errorList;
	
	private ErrorHandlerDto defaultHandler;
	
	private boolean disabled;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPumaTaskName() {
		return pumaTaskName;
	}

	public void setPumaTaskName(String pumaTaskName) {
		this.pumaTaskName = pumaTaskName;
	}

	public String getSyncServerName() {
		return syncServerName;
	}

	public void setSyncServerName(String syncServerName) {
		this.syncServerName = syncServerName;
	}

	public String getDstDBInstanceName() {
		return dstDBInstanceName;
	}

	public void setDstDBInstanceName(String dstDBInstanceName) {
		this.dstDBInstanceName = dstDBInstanceName;
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

	public boolean isDdl() {
		return ddl;
	}

	public void setDdl(boolean ddl) {
		this.ddl = ddl;
	}

	public boolean isDml() {
		return dml;
	}

	public void setDml(boolean dml) {
		this.dml = dml;
	}

	public boolean isTransaction() {
		return transaction;
	}

	public void setTransaction(boolean transaction) {
		this.transaction = transaction;
	}

	public ErrorHandlerDto getDefaultHandler() {
		return defaultHandler;
	}

	public void setDefaultHandler(ErrorHandlerDto defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public MysqlMappingDto getMysqlMapping() {
		return mysqlMapping;
	}

	public void setMysqlMapping(MysqlMappingDto mysqlMapping) {
		this.mysqlMapping = mysqlMapping;
	}

	public ErrorListDto getErrorList() {
		return errorList;
	}

	public void setErrorList(ErrorListDto errorList) {
		this.errorList = errorList;
	}

	public boolean isConsistent() {
		return consistent;
	}

	public void setConsistent(boolean consistent) {
		this.consistent = consistent;
	}
	
	
}
