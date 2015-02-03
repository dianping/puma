package com.dianping.puma.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.replicate.model.config.ServerConfig;

public interface ServerConfigService {

	ObjectId save(ServerConfig serverConfig);
	
	void modify(ServerConfig serverConfig);
	
	void remove(ObjectId id);
	
	ServerConfig find(ObjectId id);
	
	List<ServerConfig> findAll();
	
	ServerConfig find(String host);
}
