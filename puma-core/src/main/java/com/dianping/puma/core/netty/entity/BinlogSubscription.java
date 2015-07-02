package com.dianping.puma.core.netty.entity;

import java.util.List;

public class BinlogSubscription {

	private String clientName;

	private boolean ddl;

	private boolean dml;

	private boolean transaction;

	private String database;

	private List<String> tables;

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
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

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public List<String> getTables() {
		return tables;
	}

	public void setTables(List<String> tables) {
		this.tables = tables;
	}
}
