package com.dianping.puma.storage.channel;

import java.util.HashSet;
import java.util.Set;

import com.dianping.puma.storage.EventChannel;

public abstract class AbstractEventChannel implements EventChannel {

	protected String database;

	protected Set<String> tables;

	protected boolean withTransaction = true;

	protected boolean withDdl = false;

	protected boolean withDml = true;

	@Override
	public EventChannel withDatabase(String database) {
		this.database = database;
		return this;
	}

	@Override
	public EventChannel withTables(String... tables) {
		this.tables = new HashSet<String>();

		for (String table : tables) {
			this.tables.add(table);
		}
		return this;
	}

	@Override
	public EventChannel withTransaction(boolean transaction) {
		this.withTransaction = transaction;
		return this;
	}

	@Override
	public EventChannel withDdl(boolean ddl) {
		this.withDdl = ddl;
		return this;
	}

	@Override
	public EventChannel withDml(boolean dml) {
		this.withDml = dml;
		return this;
	}
}
