package com.dianping.puma.admin.model.mapper;

import com.dianping.puma.admin.model.PumaServerDto;
import com.dianping.puma.biz.entity.old.PumaServer;

public class PumaServerMapper {

	public static PumaServer convertToPumaServer(PumaServerDto pumaServerDto){
		PumaServer pumaServer = new PumaServer();
		return convertToPumaServer(pumaServer,pumaServerDto);
	}
	
	public static PumaServer convertToPumaServer(PumaServer pumaServer,PumaServerDto pumaServerDto){
		pumaServer.setName(pumaServerDto.getName());
		pumaServer.setHost(pumaServerDto.getHost());
		pumaServer.setPort(pumaServerDto.getPort());
		return pumaServer;
	}
	
	public static PumaServerDto convertToPumaServerDto(PumaServer pumaServer){
		PumaServerDto pumaServerDto = new PumaServerDto();
		pumaServerDto.setName(pumaServer.getName());
		pumaServerDto.setHost(pumaServer.getHost());
		pumaServerDto.setPort(pumaServer.getPort());
		return pumaServerDto;
	}
}
