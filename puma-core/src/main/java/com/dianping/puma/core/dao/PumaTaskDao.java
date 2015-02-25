package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.PumaTaskEntity;

import java.util.List;

public interface PumaTaskDao {

	PumaTaskEntity find(String id);

	List<PumaTaskEntity> findAll();

	void create(PumaTaskEntity entity);

	void update(PumaTaskEntity entity);

	void remove(String id);
}
