package com.dianping.puma.admin.service;

import com.dianping.puma.core.entity.PumaServerEntity;

import java.util.List;

public interface PumaServerService {

	PumaServerEntity find(String id);

	List<PumaServerEntity> findAll();

	void create(PumaServerEntity entity);

	void update(PumaServerEntity entity);

	void remove(String id);
}
