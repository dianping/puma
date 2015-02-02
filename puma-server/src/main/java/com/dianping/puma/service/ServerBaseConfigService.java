package com.dianping.puma.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.server.model.ServerBaseConfig;

public interface ServerBaseConfigService {

	ObjectId save(ServerBaseConfig pumaServerBaseConfig);
	
	void modify(ServerBaseConfig pumaServerBaseConfig);
	
	void remove(ObjectId id);
	
	ServerBaseConfig find(ObjectId id);
	
	List<ServerBaseConfig> findAll();
	
	ServerBaseConfig find(String host);
}
