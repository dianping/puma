package com.dianping.puma.admin.model.mapper;

import com.dianping.puma.admin.model.SyncServerDto;
import com.dianping.puma.biz.entity.old.SyncServer;

public class SyncServerMapper {
	
	public static SyncServer convertToSyncServer(SyncServerDto syncServerDto){
		SyncServer syncServer = new SyncServer();
		return convertToSyncServer(syncServer,syncServerDto);
	}
	
	public static SyncServer convertToSyncServer(SyncServer syncServer,SyncServerDto syncServerDto){
		syncServer.setName(syncServerDto.getName());
		syncServer.setHost(syncServerDto.getHost());
		syncServer.setPort(syncServerDto.getPort());
		return syncServer;
	}
	
	public static SyncServerDto convertToSyncServerDto(SyncServer syncServer){
		SyncServerDto syncServerDto = new SyncServerDto();
		syncServerDto.setName(syncServer.getName());
		syncServerDto.setHost(syncServer.getHost());
		syncServerDto.setPort(syncServer.getPort());
		return syncServerDto;
	}
}
