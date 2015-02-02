package com.dianping.puma.admin.service.impl;

import com.dianping.puma.admin.service.ServerConfigService;
import com.dianping.puma.core.replicate.dao.config.ServerConfigDao;
import com.dianping.puma.core.replicate.model.config.ServerConfig;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ServerConfigService")
public class ServerConfigServiceImpl implements ServerConfigService {

	@Autowired
	ServerConfigDao serverConfigDao;

	@Override
	public ObjectId save(ServerConfig serverConfig) {
		Key<ServerConfig> key = this.serverConfigDao.save(serverConfig);
		this.serverConfigDao.getDatastore().ensureIndexes();
		return (ObjectId) key.getId();
	}

	@Override
	public List<ServerConfig> findAll() {
		Query<ServerConfig> q = serverConfigDao.getDatastore().createQuery(ServerConfig.class);
		QueryResults<ServerConfig> result = serverConfigDao.find(q);
		return result.asList();
	}

	@Override
	public ServerConfig find(String name) {
		Query<ServerConfig> q = serverConfigDao.getDatastore().createQuery(ServerConfig.class);
		q.field("name").equal(name);
		return serverConfigDao.findOne(q);
	}
}
