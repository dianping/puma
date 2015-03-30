package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.PumaServer;

import java.util.List;

public interface PumaServerService {
	
	PumaServer find(long id);

	PumaServer find(String name);

	PumaServer findByHost(String host);

	List<PumaServer> findAll();

	void create(PumaServer pumaServer);

	void update(PumaServer pumaServer);

	void remove(String name);
	
	void remove(long id);
}
