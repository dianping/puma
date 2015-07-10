package com.dianping.puma.biz.entity.old;

import com.dianping.puma.biz.entity.sync.mapping.MysqlMapping;

public abstract class AbstractBaseSyncTask extends BaseSyncTask {

	private String pumaClientName;

	private long pumaClientServerId;

	private boolean ddl;

	private boolean dml;

	private boolean transaction;
	
	private boolean consistent;

	private MysqlMapping mysqlMapping;

	public String getPumaClientName() {
		return pumaClientName;
	}

	public void setPumaClientName(String pumaClientName) {
		this.pumaClientName = pumaClientName;
	}

	public long getPumaClientServerId() {
		return pumaClientServerId;
	}

	public void setPumaClientServerId(long pumaClientServerId) {
		this.pumaClientServerId = pumaClientServerId;
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

	public MysqlMapping getMysqlMapping() {
		return mysqlMapping;
	}

	public void setMysqlMapping(MysqlMapping mysqlMapping) {
		this.mysqlMapping = mysqlMapping;
	}

	public boolean isConsistent() {
		return consistent;
	}

	public void setConsistent(boolean consistent) {
		this.consistent = consistent;
	}
}
