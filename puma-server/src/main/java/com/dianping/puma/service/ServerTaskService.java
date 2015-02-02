package com.dianping.puma.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.server.model.ServerTask;

public interface ServerTaskService {
	
	ObjectId save(ServerTask pumaSyncServerConfig);
	
	void modify(ServerTask pumaSyncServerConfig);

	void remove(ObjectId id);
	
	ServerTask find(ObjectId id);
	
	List<ServerTask> find(String host);
	
	ServerTask find(long taskId);
	
	List<ServerTask> findAll();
	
	
}
