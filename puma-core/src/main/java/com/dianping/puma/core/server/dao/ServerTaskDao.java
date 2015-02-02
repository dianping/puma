package com.dianping.puma.core.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.server.model.ServerTask;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.google.code.morphia.dao.BasicDAO;

@Service("serverTaskDao")
public class ServerTaskDao extends BasicDAO<ServerTask, String> {

	@Autowired
	public ServerTaskDao(MongoClient mongoClient){
		super(mongoClient.getDatastore());
	}
}
