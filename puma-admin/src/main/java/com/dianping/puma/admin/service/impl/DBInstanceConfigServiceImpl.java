package com.dianping.puma.admin.service.impl;

import com.dianping.puma.admin.service.DBInstanceConfigService;
import com.dianping.puma.core.replicate.dao.config.DBInstanceConfigDao;
import com.dianping.puma.core.replicate.model.config.DBInstanceConfig;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dbInstanceConfigService")
public class DBInstanceConfigServiceImpl implements DBInstanceConfigService {

	@Autowired
	DBInstanceConfigDao dbInstanceConfigDao;

	@Override
	public ObjectId save(DBInstanceConfig dbInstanceConfig) {
		Key<DBInstanceConfig> key = this.dbInstanceConfigDao.save(dbInstanceConfig);
		this.dbInstanceConfigDao.getDatastore().ensureIndexes();
		return (ObjectId) key.getId();
	}

	@Override
	public List<DBInstanceConfig> findAll() {
		Query<DBInstanceConfig> q = dbInstanceConfigDao.getDatastore().createQuery(DBInstanceConfig.class);
		QueryResults<DBInstanceConfig> result = dbInstanceConfigDao.find(q);
		return result.asList();
	}

	@Override
	public DBInstanceConfig find(String name) {
		Query<DBInstanceConfig> q = dbInstanceConfigDao.getDatastore().createQuery(DBInstanceConfig.class);
		q.field("name").equal(name);
		return dbInstanceConfigDao.findOne(q);
	}
}
