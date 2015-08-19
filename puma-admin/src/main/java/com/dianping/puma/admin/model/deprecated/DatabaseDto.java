package com.dianping.puma.admin.model.deprecated;

public class DatabaseDto {
	
	private String database;
	
	private String tables;
	
	public DatabaseDto(){
		
	}
	
	public DatabaseDto(String database, String tables){
		this.database = database;
		this.tables = tables;
	}

	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getTables() {
		return tables;
	}
	public void setTables(String tables) {
		this.tables = tables;
	}
	
}
