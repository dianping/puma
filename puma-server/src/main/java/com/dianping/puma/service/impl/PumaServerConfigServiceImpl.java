package com.dianping.puma.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.server.dao.PumaServerDetailConfigDao;
import com.dianping.puma.core.server.model.PumaServerDetailConfig;
import com.dianping.puma.service.PumaServerConfigService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("pumaServerConfigService")
public class PumaServerConfigServiceImpl implements PumaServerConfigService {

	@Autowired
	PumaServerDetailConfigDao pumaServerDetailConfigDao;

	public void setPumaServerDetailConfigDao(PumaServerDetailConfigDao pumaServerDetailConfigDao){
		this.pumaServerDetailConfigDao=pumaServerDetailConfigDao;
	}
	
	@Override
	public ObjectId save(PumaServerDetailConfig pumaServerDetailConfig) {
		Key<PumaServerDetailConfig> key = this.pumaServerDetailConfigDao
				.save(pumaServerDetailConfig);
		return (ObjectId) key.getId();
	}

	@Override
	public void modify(PumaServerDetailConfig pumaServerDetailConfig) {
		this.pumaServerDetailConfigDao.save(pumaServerDetailConfig);
	}

	@Override
	public void remove(ObjectId id) {
		Query<PumaServerDetailConfig> q = this.pumaServerDetailConfigDao
				.getDatastore().createQuery(PumaServerDetailConfig.class);
		q.field("_id").equal(id);
		this.pumaServerDetailConfigDao.deleteByQuery(q);
	}

	@Override
	public PumaServerDetailConfig find(ObjectId id) {
		return this.pumaServerDetailConfigDao.getDatastore().getByKey(
				PumaServerDetailConfig.class,
				new Key<PumaServerDetailConfig>(PumaServerDetailConfig.class,
						id));
	}

	@Override
	public List<PumaServerDetailConfig> find(String webAppName) {
		Query<PumaServerDetailConfig> q = this.pumaServerDetailConfigDao
				.getDatastore().createQuery(PumaServerDetailConfig.class);
		q.field("webAppName").equal(webAppName);
		QueryResults<PumaServerDetailConfig> result = this.pumaServerDetailConfigDao
				.find(q);
		return result.asList();
	}

	@Override
	public List<PumaServerDetailConfig> findAll() {
		Query<PumaServerDetailConfig> q = this.pumaServerDetailConfigDao
				.getDatastore().createQuery(PumaServerDetailConfig.class);
		QueryResults<PumaServerDetailConfig> result = this.pumaServerDetailConfigDao
				.find(q);
		return result.asList();
	}
}
