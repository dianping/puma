package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.PumaTaskEntity;

import java.util.List;

public interface PumaTaskService {

	PumaTaskEntity find(String id);

	List<PumaTaskEntity> findByPumaServerName(String pumaServerName);

	List<PumaTaskEntity> findAll();

	void create(PumaTaskEntity entity);

	void update(PumaTaskEntity entity);

	void remove(String id);
}
