package com.dianping.puma.core.replicate.dao.config;

import com.dianping.puma.core.replicate.model.config.DBInstanceConfig;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.google.code.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dbInstanceConfigDao")
public class DBInstanceConfigDao extends BasicDAO<DBInstanceConfig, String> {

	@Autowired
	public DBInstanceConfigDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}
}
