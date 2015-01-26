package com.dianping.puma.core.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.server.model.PumaServerDetailConfig;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.google.code.morphia.dao.BasicDAO;

@Service("pumaServerDetailConfigDao")
public class PumaServerDetailConfigDao extends BasicDAO<PumaServerDetailConfig, String> {

	@Autowired
	public PumaServerDetailConfigDao(MongoClient mongoClient){
		super(mongoClient.getDatastore());
	}
}
