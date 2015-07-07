package com.dianping.puma.admin.model.mapper;

import com.dianping.puma.admin.model.DBInstanceDto;
import com.dianping.puma.admin.model.DstDBInstanceDto;
import com.dianping.puma.admin.model.SrcDBInstanceDto;
import com.dianping.puma.biz.entity.DBInstance;
import com.dianping.puma.biz.entity.DstDBInstance;
import com.dianping.puma.biz.entity.SrcDBInstance;

public class DBInstanceMapper {

	public static DBInstanceDto convertToDBInstanceDto(DBInstance dbInstance){
		DBInstanceDto dbInstanceDto = null;
		if(dbInstance instanceof DstDBInstance){
			dbInstanceDto  = new DstDBInstanceDto();
		}else if(dbInstance instanceof SrcDBInstance){
			dbInstanceDto  = new SrcDBInstanceDto();
		}else{
			return dbInstanceDto;
		}
		dbInstanceDto.setServerId(dbInstance.getServerId());
		dbInstanceDto.setName(dbInstance.getName());
		dbInstanceDto.setHost(dbInstance.getHost());
		dbInstanceDto.setPort(dbInstance.getPort());
		dbInstanceDto.setUsername(dbInstance.getUsername());
		dbInstanceDto.setPassword(dbInstance.getPassword());
		return dbInstanceDto;
	}
	
	public static DBInstance convertToDBInstance(DBInstance dbInstance , DBInstanceDto dbInstanceDto){
		dbInstance.setName(dbInstanceDto.getName());
		dbInstance.setServerId(dbInstanceDto.getServerId());
		dbInstance.setHost(dbInstanceDto.getHost());
		dbInstance.setPort(dbInstanceDto.getPort());
		dbInstance.setUsername(dbInstanceDto.getUsername());
		dbInstance.setPassword(dbInstanceDto.getPassword());
		dbInstance.setMetaHost(dbInstanceDto.getHost());
		dbInstance.setMetaPort(dbInstanceDto.getPort());
		dbInstance.setMetaUsername(dbInstanceDto.getUsername());
		dbInstance.setMetaPassword(dbInstanceDto.getPassword());
		return dbInstance;
	}

	public static DBInstance convertToDBInstance(DBInstanceDto dbInstanceDto){
		DBInstance dbInstance = null;
		if(dbInstanceDto instanceof DstDBInstanceDto){
			dbInstance  = new DstDBInstance();
		}else if(dbInstanceDto instanceof SrcDBInstanceDto){
			dbInstance  = new SrcDBInstance();
		}else{
			return dbInstance;
		}
		dbInstance.setName(dbInstanceDto.getName());
		dbInstance.setServerId(dbInstanceDto.getServerId());
		dbInstance.setHost(dbInstanceDto.getHost());
		dbInstance.setPort(dbInstanceDto.getPort());
		dbInstance.setUsername(dbInstanceDto.getUsername());
		dbInstance.setPassword(dbInstanceDto.getPassword());
		dbInstance.setMetaHost(dbInstanceDto.getHost());
		dbInstance.setMetaPort(dbInstanceDto.getPort());
		dbInstance.setMetaUsername(dbInstanceDto.getUsername());
		dbInstance.setMetaPassword(dbInstanceDto.getPassword());
		return dbInstance;
	}
}
