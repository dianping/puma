package com.dianping.puma.admin.model.mapper;

import com.dianping.puma.admin.model.ErrorHandlerDto;

import com.dianping.puma.admin.model.SyncTaskDto;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.model.BinlogInfo;

public class SyncTaskMapper {

	public static SyncTask convertToSyncTask(SyncTaskDto syncTaskDto) {
		SyncTask syncTask = new SyncTask();
		convertToSyncTask(syncTask, syncTaskDto);
		return syncTask;
	}

	public static SyncTaskDto convertToSyncTaskDto(SyncTask syncTask) {
		if (syncTask == null) {
			return null;
		}
		SyncTaskDto syncTaskDto = new SyncTaskDto();
		syncTaskDto.setName(syncTask.getName());
		syncTaskDto.setDstDBInstanceName(syncTask.getDstDBInstanceName());
		syncTaskDto.setPumaTaskName(syncTask.getPumaTaskName());
		syncTaskDto.setSyncServerName(syncTask.getSyncServerName());
		syncTaskDto.setBinlogFile(syncTask.getBinlogInfo().getBinlogFile());
		syncTaskDto.setBinlogPosition(syncTask.getBinlogInfo().getBinlogPosition());
		syncTaskDto.setDdl(syncTask.isDdl());
		syncTaskDto.setDml(syncTask.isDml());
		syncTaskDto.setConsistent(syncTask.isConsistent());
		syncTaskDto.setTransaction(syncTask.isTransaction());
		ErrorHandlerDto deaultHandler = new ErrorHandlerDto();
		deaultHandler.setName(syncTask.getDefaultHandler());
		deaultHandler.setDesc(syncTask.getDefaultHandler());
		syncTaskDto.setDefaultHandler(deaultHandler);
		syncTaskDto.setErrorList(ErrorListMapper.convertToErrorList(syncTask.getErrorCodeHandlerNameMap()));
		syncTaskDto.setMysqlMapping(MysqlMappingMapper.convertToMysqlMapping(syncTask.getMysqlMapping()));
		syncTaskDto.setDisabled(true);
		return syncTaskDto;
	}

	public static SyncTask convertToSyncTask(SyncTask syncTask, SyncTaskDto syncTaskDto) {
		syncTask.setName(syncTaskDto.getName());
		syncTask.setPumaClientName(syncTaskDto.getName());
		syncTask.setDstDBInstanceName(syncTaskDto.getDstDBInstanceName());
		syncTask.setPumaTaskName(syncTaskDto.getPumaTaskName());
		syncTask.setSyncServerName(syncTaskDto.getSyncServerName());
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile(syncTaskDto.getBinlogFile());
		binlogInfo.setBinlogPosition(syncTaskDto.getBinlogPosition());
		syncTask.setBinlogInfo(binlogInfo);
		syncTask.setDml(syncTaskDto.isDml());
		syncTask.setDdl(syncTaskDto.isDdl());
		syncTask.setTransaction(syncTaskDto.isTransaction());
		syncTask.setConsistent(syncTaskDto.isConsistent());
		syncTask.setSyncType(SyncType.SYNC);
		syncTask.setDefaultHandler(syncTaskDto.getDefaultHandler().getName());
		syncTask.setMysqlMapping(MysqlMappingMapper.convertToMysqlMapping(syncTaskDto.getMysqlMapping()));
		syncTask.setErrorCodeHandlerNameMap(ErrorListMapper.convertToErrors(syncTaskDto.getErrorList()));
		return syncTask;
	}

}
