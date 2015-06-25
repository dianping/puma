package com.dianping.puma.core.netty.entity;

import com.dianping.puma.core.model.BinlogInfo;

public class BinlogQuery {

	private String clientName;
	private String target;
	private long serverId;

	private boolean dml;
	private boolean ddl;
	private boolean transaction;

	private long seq;
	private BinlogInfo binlogInfo;
	private long timestamp;

	private String[] databaseTables;

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public boolean isDml() {
		return dml;
	}

	public void setDml(boolean dml) {
		this.dml = dml;
	}

	public boolean isDdl() {
		return ddl;
	}

	public void setDdl(boolean ddl) {
		this.ddl = ddl;
	}

	public boolean isTransaction() {
		return transaction;
	}

	public void setTransaction(boolean transaction) {
		this.transaction = transaction;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String[] getDatabaseTables() {
		return databaseTables;
	}

	public void setDatabaseTables(String[] databaseTables) {
		this.databaseTables = databaseTables;
	}
}
