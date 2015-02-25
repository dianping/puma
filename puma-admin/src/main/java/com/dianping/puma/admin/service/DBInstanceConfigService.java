package com.dianping.puma.admin.service;


import com.dianping.puma.core.entity.SrcDBInstanceEntity;

import java.util.List;

public interface DBInstanceConfigService {

	/*
	ObjectId save(DBInstanceConfig dbInstanceConfig);

	List<DBInstanceConfig> findAll();

	DBInstanceConfig find(String name);
	*/

	SrcDBInstanceEntity findById(String id);

	SrcDBInstanceEntity findByName(String name);

	List<SrcDBInstanceEntity> findAll();

	void create(SrcDBInstanceEntity config);
}
