package com.dianping.puma.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.server.model.PumaServerDetailConfig;

public interface PumaServerConfigService {
	
	ObjectId save(PumaServerDetailConfig pumaSyncServerConfig);
	
	void modify(PumaServerDetailConfig pumaSyncServerConfig);

	void remove(ObjectId id);
	
	PumaServerDetailConfig find(ObjectId id);
	
	List<PumaServerDetailConfig> find(String webAppId);
	
	List<PumaServerDetailConfig> findAll();
	
	
}
