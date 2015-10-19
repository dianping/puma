package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaServerEntity;

import java.util.List;

public interface PumaServerService {

	PumaServerEntity findById(int id);

	PumaServerEntity find(String name);

	PumaServerEntity findByHost(String host);

	List<PumaServerEntity> findOnCurrentServer();

	List<PumaServerEntity> findAll();

	List<PumaServerEntity> findAllAlive();

	long count();

	List<PumaServerEntity> findByPage(int page, int pageSize);

	void registerByHost(String host);

	void create(PumaServerEntity pumaServer);

	void update(PumaServerEntity pumaServer);

	void remove(String name);

	void remove(int id);
}
