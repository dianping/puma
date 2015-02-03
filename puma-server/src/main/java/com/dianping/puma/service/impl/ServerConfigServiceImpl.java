package com.dianping.puma.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.replicate.dao.config.ServerConfigDao;
import com.dianping.puma.core.replicate.model.config.ServerConfig;
import com.dianping.puma.service.ServerConfigService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("serverConfigService")
public class ServerConfigServiceImpl implements ServerConfigService {

	@Autowired
	private ServerConfigDao serverConfigDao;

	public void setServerBaseConfigDao(ServerConfigDao serverConfigDao) {
		this.serverConfigDao = serverConfigDao;
	}

	@Override
	public ServerConfig find(ObjectId id) {
		return this.serverConfigDao.getDatastore().getByKey(
				ServerConfig.class,
				new Key<ServerConfig>(ServerConfig.class, id));
	}

	@Override
	public ServerConfig find(String host) {
		Query q = this.serverConfigDao.getDatastore().createQuery(
				ServerConfig.class);
		q.field("host").equals(host);
		return this.serverConfigDao.findOne(q);
	}

	@Override
	public List<ServerConfig> findAll() {
		Query<ServerConfig> q = this.serverConfigDao.getDatastore()
				.createQuery(ServerConfig.class);
		QueryResults<ServerConfig> result = this.serverConfigDao
				.find(q);
		return result.asList();
	}

	@Override
	public void modify(ServerConfig serverConfig) {
		this.serverConfigDao.save(serverConfig);
	}
	
	@Override
	public void remove(ObjectId id) {
		Query<ServerConfig> q = this.serverConfigDao.getDatastore()
				.createQuery(ServerConfig.class);
		q.field("_id").equal(id);
		serverConfigDao.deleteByQuery(q);
	}

	@Override
	public ObjectId save(ServerConfig serverConfig) {
		Key<ServerConfig> key = serverConfigDao.save(serverConfig);
		return (ObjectId) key.getId();
	}

}
