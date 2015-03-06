package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.PumaServer;

import java.util.List;

public interface PumaServerService {

	PumaServer find(String id);

	PumaServer findByName(String name);

	PumaServer findByHostAndPort(String host, Integer port);

	List<PumaServer> findAll();

	void create(PumaServer entity);

	void update(PumaServer entity);

	void remove(String id);
}
