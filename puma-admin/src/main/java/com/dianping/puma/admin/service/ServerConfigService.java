package com.dianping.puma.admin.service;

import com.dianping.puma.core.replicate.model.config.ServerConfig;
import org.bson.types.ObjectId;

import java.util.List;

public interface ServerConfigService {

	ObjectId save(ServerConfig serverConfig);

	List<ServerConfig> findAll();

	ServerConfig find(String name);
}
