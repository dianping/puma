package com.dianping.puma.taskexecutor.task;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

public class DatabaseTask {

	private String database;

	private List<String> tables;

	private Date beginTime;

	public DatabaseTask() {}

	public DatabaseTask(String database, List<String> tables, Date beginTime) {
		this.database = database;
		this.tables = tables;
		this.beginTime = beginTime;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("database", database)
				.append("tables", tables)
				.append("beginTime", beginTime)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DatabaseTask))
			return false;

		DatabaseTask that = (DatabaseTask) o;

		if (beginTime != null ? !beginTime.equals(that.beginTime) : that.beginTime != null)
			return false;
		if (!database.equals(that.database))
			return false;
		return tables.equals(that.tables);

	}

	@Override
	public int hashCode() {
		int result = database.hashCode();
		result = 31 * result + tables.hashCode();
		result = 31 * result + (beginTime != null ? beginTime.hashCode() : 0);
		return result;
	}

	public boolean equalName(DatabaseTask databaseTask) {
		return database.equals(databaseTask.getDatabase());
	}

	public String getDatabase() {
		return database;
	}

	public List<String> getTables() {
		return tables;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setTables(List<String> tables) {
		this.tables = tables;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
}
