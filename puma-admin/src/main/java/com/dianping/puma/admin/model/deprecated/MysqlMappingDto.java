package com.dianping.puma.admin.model.deprecated;

import com.dianping.puma.admin.model.deprecated.DatabaseMappingDto;

import java.util.List;

public class MysqlMappingDto {

	private List<DatabaseMappingDto> databaseMappings;

	
	public MysqlMappingDto(){
		
	}
	
	public List<DatabaseMappingDto> getDatabaseMappings() {
		return databaseMappings;
	}

	public void setDatabaseMappings(List<DatabaseMappingDto> databaseMappings) {
		this.databaseMappings = databaseMappings;
	} 
}
