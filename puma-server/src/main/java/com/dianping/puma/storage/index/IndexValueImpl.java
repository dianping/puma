package com.dianping.puma.storage.index;

import com.dianping.puma.storage.Sequence;

public class IndexValueImpl implements IndexValue<IndexKeyImpl> {

	private String database;
	
	private String table;
	
	private boolean isDdl;
	
	private boolean isDml;
	
	private boolean isTransactionBegin;
	
	private boolean isTransactionCommit;
	
	private Sequence sequence;
	
	private IndexKeyImpl indexKey;
	
	public void setIndexKey(IndexKeyImpl indexKey) {
		this.indexKey = indexKey;
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
		return isTransactionBegin || isTransactionCommit;
	}

	public boolean isTransactionBegin() {
		return isTransactionBegin;
	}

	public void setTransactionBegin(boolean isTransactionBegin) {
		this.isTransactionBegin = isTransactionBegin;
	}

	public boolean isTransactionCommit() {
		return isTransactionCommit;
	}

	public void setTransactionCommit(boolean isTransactionCommit) {
		this.isTransactionCommit = isTransactionCommit;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	@Override
   public IndexKeyImpl getIndexKey() {
	   return this.indexKey;
   }
}
