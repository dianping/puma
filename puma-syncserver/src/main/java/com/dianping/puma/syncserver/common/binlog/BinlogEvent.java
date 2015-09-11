package com.dianping.puma.syncserver.common.binlog;

import com.dianping.puma.syncserver.executor.transform.rule.DataSourceMappingRule;
import com.dianping.puma.syncserver.executor.transform.rule.DbTbMappingRule;
import org.apache.commons.lang3.tuple.Pair;

import javax.sql.DataSource;

public abstract class BinlogEvent {

	protected String database;

	protected String table;

	protected String sql;

	protected Object[] params;

	protected EventType eventType;

	protected DataSource dataSource;

	public abstract DataSource mapDataSource(DataSourceMappingRule rule);

	public abstract Pair<String, String> mapDbTb(DbTbMappingRule rule);

	public abstract void buildSql();

	public abstract void buildParams();

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

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
