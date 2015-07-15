package com.dianping.puma.biz.service;

import java.util.List;

import com.dianping.puma.biz.entity.PumaServerEntity;

public interface PumaServerService {

	PumaServerEntity find(int id);

	PumaServerEntity find(String name);

	PumaServerEntity findByHost(String host);

	List<PumaServerEntity> findAll();

	long count();

	List<PumaServerEntity> findByPage(int page, int pageSize);

	void heartBeat();

	void create(PumaServerEntity pumaServer);

	void update(PumaServerEntity pumaServer);

	void remove(String name);

	void remove(int id);
}
