package com.dianping.puma.biz.entity;

public class MappingEntity {

	private int id;

	private String databaseFrom;

	private String databaseTo;

	private String tableFrom;

	private String tableTo;

	private String columnFrom;

	private String columnTo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDatabaseFrom() {
		return databaseFrom;
	}

	public void setDatabaseFrom(String databaseFrom) {
		this.databaseFrom = databaseFrom;
	}

	public String getDatabaseTo() {
		return databaseTo;
	}

	public void setDatabaseTo(String databaseTo) {
		this.databaseTo = databaseTo;
	}

	public String getTableFrom() {
		return tableFrom;
	}

	public void setTableFrom(String tableFrom) {
		this.tableFrom = tableFrom;
	}

	public String getTableTo() {
		return tableTo;
	}

	public void setTableTo(String tableTo) {
		this.tableTo = tableTo;
	}

	public String getColumnFrom() {
		return columnFrom;
	}

	public void setColumnFrom(String columnFrom) {
		this.columnFrom = columnFrom;
	}

	public String getColumnTo() {
		return columnTo;
	}

	public void setColumnTo(String columnTo) {
		this.columnTo = columnTo;
	}
}
