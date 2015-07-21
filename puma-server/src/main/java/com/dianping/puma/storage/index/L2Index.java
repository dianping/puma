package com.dianping.puma.storage.index;

import com.dianping.puma.storage.Sequence;

public class L2Index {

	private String database;
	
	private String table;
	
	private boolean isDdl;
	
	private boolean isDml;
	
	private boolean isTransaction;
	
	private Sequence sequence;
	
	private BinlogIndexKey binlogIndexKey;
	
	public BinlogIndexKey getBinlogIndexKey() {
		return binlogIndexKey;
	}

	public void setBinlogIndexKey(BinlogIndexKey binlogIndexKey) {
		this.binlogIndexKey = binlogIndexKey;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public boolean isDdl() {
		return isDdl;
	}

	public void setDdl(boolean isDdl) {
		this.isDdl = isDdl;
	}

	public boolean isDml() {
		return isDml;
	}

	public void setDml(boolean isDml) {
		this.isDml = isDml;
	}
	
	public boolean isTransaction() {
		return isTransaction;
	}

	public void setTransaction(boolean isTransaction) {
		this.isTransaction = isTransaction;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}
}
