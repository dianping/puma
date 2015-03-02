package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.PumaTask;

import java.util.List;

public interface PumaTaskDao {

	PumaTask find(String id);

	List<PumaTask> findByPumaServerName(String pumaServerName);

	List<PumaTask> findAll();

	void create(PumaTask entity);

	void update(PumaTask entity);

	void remove(String id);
}
