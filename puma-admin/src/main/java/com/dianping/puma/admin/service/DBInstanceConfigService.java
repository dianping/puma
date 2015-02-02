package com.dianping.puma.admin.service;

import com.dianping.puma.core.replicate.model.config.DBInstanceConfig;
import org.bson.types.ObjectId;

import java.util.List;

public interface DBInstanceConfigService {

	ObjectId save(DBInstanceConfig dbInstanceConfig);

	List<DBInstanceConfig> findAll();

	DBInstanceConfig find(String name);

}
