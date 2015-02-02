package com.dianping.puma.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.server.dao.ServerBaseConfigDao;
import com.dianping.puma.core.server.dao.ServerTaskDao;
import com.dianping.puma.core.server.model.ServerBaseConfig;
import com.dianping.puma.service.ServerBaseConfigService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("serverBaseConfigService")
public class ServerBaseConfigServiceImpl implements ServerBaseConfigService {

	@Autowired
	private ServerBaseConfigDao serverBaseConfigDao;

	public void setServerBaseConfigDao(ServerBaseConfigDao serverBaseConfigDao) {
		this.serverBaseConfigDao = serverBaseConfigDao;
	}

	@Override
	public ServerBaseConfig find(ObjectId id) {
		return this.serverBaseConfigDao.getDatastore().getByKey(
				ServerBaseConfig.class,
				new Key<ServerBaseConfig>(ServerBaseConfig.class, id));
	}

	@Override
	public ServerBaseConfig find(String host) {
		Query q = this.serverBaseConfigDao.getDatastore().createQuery(
				ServerBaseConfig.class);
		q.field("host").equals(host);
		return this.serverBaseConfigDao.findOne(q);
	}

	@Override
	public List<ServerBaseConfig> findAll() {
		Query<ServerBaseConfig> q = this.serverBaseConfigDao.getDatastore()
				.createQuery(ServerBaseConfig.class);
		QueryResults<ServerBaseConfig> result = this.serverBaseConfigDao
				.find(q);
		return result.asList();
	}

	@Override
	public void modify(ServerBaseConfig serverBaseConfig) {
		this.serverBaseConfigDao.save(serverBaseConfig);
	}
	
	@Override
	public void remove(ObjectId id) {
		Query<ServerBaseConfig> q = this.serverBaseConfigDao.getDatastore()
				.createQuery(ServerBaseConfig.class);
		q.field("_id").equal(id);
		serverBaseConfigDao.deleteByQuery(q);
	}

	@Override
	public ObjectId save(ServerBaseConfig serverBaseConfig) {
		Key<ServerBaseConfig> key = serverBaseConfigDao.save(serverBaseConfig);
		return (ObjectId) key.getId();
	}

}
