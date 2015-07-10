package com.dianping.puma.admin.model.mapper;

import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.admin.model.DatabaseMappingDto;
import com.dianping.puma.admin.model.MysqlMappingDto;
import com.dianping.puma.admin.model.TableMappingDto;
import com.dianping.puma.core.dto.mapping.DatabaseMapping;
import com.dianping.puma.core.dto.mapping.MysqlMapping;
import com.dianping.puma.core.dto.mapping.TableMapping;

public class MysqlMappingMapper {
	
	public static MysqlMapping convertToMysqlMapping(MysqlMappingDto mysqlMappingDto){
		MysqlMapping mysqlMapping = new MysqlMapping();
		if(mysqlMappingDto.getDatabaseMappings() == null){
			return mysqlMapping;
		}
		List<DatabaseMapping> databaseMappings = new ArrayList<DatabaseMapping>();
		for(DatabaseMappingDto databaseMappingDto :mysqlMappingDto.getDatabaseMappings()){
			DatabaseMapping databaseMapping = new DatabaseMapping();
			databaseMapping.setFrom(databaseMappingDto.getDatabaseFrom());
			databaseMapping.setTo(databaseMappingDto.getDatabaseTo());
			List<TableMapping> tableMappings = null;
			if(databaseMappingDto.getTableMappings() != null){
				tableMappings = new ArrayList<TableMapping>();
				for(TableMappingDto tableMappingDto :databaseMappingDto.getTableMappings()){
					TableMapping tableMapping = new TableMapping();
					tableMapping.setFrom(tableMappingDto.getTableFrom());
					tableMapping.setTo(tableMappingDto.getTableTo());
					tableMappings.add(tableMapping);
				}
			}
			databaseMapping.setTables(tableMappings);
			databaseMappings.add(databaseMapping);
		}
		mysqlMapping.setDatabases(databaseMappings);
		return mysqlMapping;
	}
	
	public static MysqlMappingDto convertToMysqlMapping(MysqlMapping mysqlMapping){
		MysqlMappingDto mysqlMappingDto = new MysqlMappingDto();
		if(mysqlMapping == null){
			return mysqlMappingDto;
		}
		List<DatabaseMappingDto> databaseMapingDtos = new ArrayList<DatabaseMappingDto>();
		for(DatabaseMapping databaseMapping :mysqlMapping.getDatabases()){
			DatabaseMappingDto databaseMappingDto = new DatabaseMappingDto();
			databaseMappingDto.setDatabaseFrom(databaseMapping.getFrom());
			databaseMappingDto.setDatabaseTo(databaseMapping.getTo());
			List<TableMappingDto> tableMappingDtos = null;
			if(databaseMapping.getTables() != null){
				tableMappingDtos = new ArrayList<TableMappingDto>();
				for(TableMapping tableMapping :databaseMapping.getTables()){
					TableMappingDto tableMappingDto = new TableMappingDto();
					tableMappingDto.setTableFrom(tableMapping.getFrom());
					tableMappingDto.setTableTo(tableMapping.getTo());
					tableMappingDtos.add(tableMappingDto);
				}
			}
			databaseMappingDto.setTableMappings((tableMappingDtos));
			databaseMapingDtos.add(databaseMappingDto);
		}
		mysqlMappingDto.setDatabaseMappings(databaseMapingDtos);
		return mysqlMappingDto;
	}
}
