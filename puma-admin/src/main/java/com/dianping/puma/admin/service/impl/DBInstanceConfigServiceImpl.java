package com.dianping.puma.admin.service.impl;

import com.dianping.puma.core.entity.SrcDBInstanceEntity;

/*
@Service("dbInstanceConfigService")
public class DBInstanceConfigServiceImpl implements DBInstanceConfigService {

	@Autowired
	SrcDBInstanceDao srcDbInstanceDao;

	@Override
	public SrcDBInstanceEntity findById(String id) {
		return srcDbInstanceDao.findById(id);
	}

	@Override
	public SrcDBInstanceEntity findByName(String name) {
		return srcDbInstanceDao.findByName(name);
	}

	@Override
	public List<SrcDBInstanceEntity> findAll() {
		return srcDbInstanceDao.findAll();
	}

	@Override
	public void create(SrcDBInstanceEntity config) {
		srcDbInstanceDao.create(config);
	}

	/*
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
}*/
