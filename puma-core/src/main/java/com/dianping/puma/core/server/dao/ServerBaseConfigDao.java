package com.dianping.puma.core.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.server.model.ServerBaseConfig;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.google.code.morphia.dao.BasicDAO;

@Service("serverBaseConfigDao")
public class ServerBaseConfigDao extends BasicDAO<ServerBaseConfig,String>{

	@Autowired
	public ServerBaseConfigDao(MongoClient mongoClient){
		super(mongoClient.getDatastore());
	}
}
