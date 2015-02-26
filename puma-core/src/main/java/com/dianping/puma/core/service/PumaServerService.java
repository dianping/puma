package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.PumaServerEntity;

import java.util.List;

public interface PumaServerService {

	PumaServerEntity find(String id);

	PumaServerEntity findByHostAndPort(String host, String port);

	List<PumaServerEntity> findAll();

	void create(PumaServerEntity entity);

	void update(PumaServerEntity entity);

	void remove(String id);
}
