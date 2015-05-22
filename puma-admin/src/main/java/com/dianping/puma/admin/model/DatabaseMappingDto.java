package com.dianping.puma.admin.model;

import java.util.List;

public class DatabaseMappingDto {
	
	private String databaseFrom;
	
	private String databaseTo;
	
	private List<TableMappingDto> tableMappings;
	
	public DatabaseMappingDto(){
		
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

	public List<TableMappingDto> getTableMappings() {
		return tableMappings;
	}

	public void setTableMappings(List<TableMappingDto> tableMappings) {
		this.tableMappings = tableMappings;
	}
}
