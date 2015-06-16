package com.dianping.puma.admin.model.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.dianping.puma.admin.model.DatabaseDto;
import com.dianping.puma.admin.model.PumaServerDto;
import com.dianping.puma.admin.model.PumaTaskDto;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.model.AcceptedTables;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;

public class PumaTaskMapper {

	private static final String TABLE_TABLE_SPLIT = "&";

	public static PumaTaskDto convertToPumaTaskDto(PumaTask pumaTask) {
		if (pumaTask == null) {
			return null;
		}
		PumaTaskDto pumaTaskDto = new PumaTaskDto();
		pumaTaskDto.setSrcDBInstanceName(pumaTask.getSrcDBInstanceName());
		List<PumaServerDto> pumaServerDtos = new ArrayList<PumaServerDto>();
		if (pumaTask.getPumaServerNames() == null || pumaTask.getPumaServerNames().size() == 0) {
			PumaServerDto pumaServerDto = new PumaServerDto();
			pumaServerDto.setName(pumaTask.getPumaServerName());
			pumaServerDtos.add(pumaServerDto);
		} else {
			for (String serverName : pumaTask.getPumaServerNames()) {
				PumaServerDto pumaServerDto = new PumaServerDto();
				pumaServerDto.setName(serverName);
				pumaServerDtos.add(pumaServerDto);
			}
		}
		pumaTaskDto.setPumaServerDtos(pumaServerDtos);
		pumaTaskDto.setName(pumaTask.getName());
		pumaTaskDto.setBinlogFile(pumaTask.getBinlogInfo().getBinlogFile());
		pumaTaskDto.setBinlogPosition(pumaTask.getBinlogInfo().getBinlogPosition());
		pumaTaskDto.setPreservedDay(pumaTask.getPreservedDay());
		List<DatabaseDto> databases = new ArrayList<DatabaseDto>();
		if (pumaTask.getTableSet() != null) {
			Map<String, List<String>> tableSet = pumaTask.getTableSet().mapSchemaTables();
			for (Map.Entry<String, List<String>> entry : tableSet.entrySet()) {
				DatabaseDto databaseDto = new DatabaseDto();
				databaseDto.setDatabase(entry.getKey());
				StringBuilder strTables = new StringBuilder();
				for (String entryValue : entry.getValue()) {
					strTables.append(entryValue);
					strTables.append(TABLE_TABLE_SPLIT);
				}
				if (strTables.length() > 0) {
					strTables.deleteCharAt(strTables.length() - 1);
				}
				databaseDto.setTables(strTables.toString());
				databases.add(databaseDto);
			}
		} else {
			Map<String, AcceptedTables> acceptedTables = pumaTask.getAcceptedDataInfos();
			for (Map.Entry<String, AcceptedTables> entry : acceptedTables.entrySet()) {
				DatabaseDto databaseDto = new DatabaseDto();
				databaseDto.setDatabase(entry.getKey());
				StringBuilder strTables = new StringBuilder();
				for (String entryValue : entry.getValue().getTables()) {
					strTables.append(entryValue);
					strTables.append(TABLE_TABLE_SPLIT);
				}
				if (strTables.length() > 0) {
					strTables.deleteCharAt(strTables.length() - 1);
				}
				databaseDto.setTables(strTables.toString());
				databases.add(databaseDto);
			}
		}

		pumaTaskDto.setDatabases(databases);
		pumaTaskDto.setDisabled(true);
		pumaTaskDto.setIsShow(true);
		return pumaTaskDto;
	}

	public static PumaTask convertToPumaTask(PumaTaskDto pumaTaskDto) {
		PumaTask pumaTask = new PumaTask();
		return convertToPumaTask(pumaTask, pumaTaskDto);
	}

	public static PumaTask convertToPumaTask(PumaTask pumaTask, PumaTaskDto pumaTaskDto) {
		pumaTask.setName(pumaTaskDto.getName());
		pumaTask.setSrcDBInstanceName(pumaTaskDto.getSrcDBInstanceName());
		List<String> serverNames = new ArrayList<String>();
		for (PumaServerDto pumaServerDto : pumaTaskDto.getPumaServerDtos()) {
			serverNames.add(pumaServerDto.getName());
		}
		pumaTask.setPumaServerNames(serverNames);
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile(pumaTaskDto.getBinlogFile());
		binlogInfo.setBinlogPosition(pumaTaskDto.getBinlogPosition());
		pumaTask.setBinlogInfo(binlogInfo);
		pumaTask.setPreservedDay(pumaTaskDto.getPreservedDay());
		List<DatabaseDto> databases = pumaTaskDto.getDatabases();
		if (databases != null && databases.size() > 0) {
			TableSet tableSet = new TableSet();
			for (DatabaseDto database : databases) {
				String tables[] = StringUtils.split(database.getTables(), "&");
				if (tables != null) {
					for (int j = 0; j != tables.length; ++j) {
						tableSet.add(new Table(database.getDatabase(), tables[j]));
					}
				}
			}
			pumaTask.setTableSet(tableSet);
			Map<String, AcceptedTables> acceptedDataInfos = new HashMap<String, AcceptedTables>();
			for (DatabaseDto database : databases) {
				String tables[] = StringUtils.split(database.getTables(), "&");
				if (tables != null) {
					AcceptedTables acceptedTablses = new AcceptedTables();
					List<String> tblList = new ArrayList<String>();
					for (int j = 0; j != tables.length; ++j) {
						tblList.add(tables[j]);
					}
					acceptedTablses.setTables(tblList);
					acceptedDataInfos.put(database.getDatabase(), acceptedTablses);
				}
			}
			pumaTask.setAcceptedDataInfos(acceptedDataInfos);
		}
		return pumaTask;
	}

}
