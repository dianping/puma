package com.dianping.puma.core.replicate.dao.config;

import com.dianping.puma.core.replicate.model.config.ServerConfig;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.google.code.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("serverConfigDao")
public class ServerConfigDao extends BasicDAO<ServerConfig, String> {

	@Autowired
	public ServerConfigDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}
}
