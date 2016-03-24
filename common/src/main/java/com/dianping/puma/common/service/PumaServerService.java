package com.dianping.puma.common.service;

import com.dianping.puma.common.model.PumaServer;

import java.util.List;

public interface PumaServerService {

	PumaServer findById(int id);

	PumaServer find(String name);

	PumaServer findByHost(String host);

	List<PumaServer> findOnCurrentServer();

	List<PumaServer> findAll();

	List<PumaServer> findAllAlive();

	long count();

	List<PumaServer> findByPage(int page, int pageSize);

	void registerByHost(String host);

	void create(PumaServer pumaServer);

	void update(PumaServer pumaServer);

	void remove(String name);

	void remove(int id);
}
