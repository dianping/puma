package com.dianping.puma.core.model;

import java.util.List;

public class AcceptedDataInfo {
	private String database;
	private List<String> tables;
	
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getDatabase() {
		return database;
	}
	
	public void setTables(List<String> tables) {
		this.tables = tables;
	}
	public List<String> getTables() {
		return tables;
	}
	
}
