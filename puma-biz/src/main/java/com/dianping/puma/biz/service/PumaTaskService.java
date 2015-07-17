package com.dianping.puma.biz.service;

import java.util.List;

import com.dianping.puma.biz.entity.PumaTaskEntity;

public interface PumaTaskService {

	PumaTaskEntity findById(int id);

	PumaTaskEntity findByName(String name);

	List<PumaTaskEntity> findByPumaServerName(String pumaServerName);

	List<PumaTaskEntity> findAll();

	List<PumaTaskEntity> findByPage(int page, int pageSize);

	long count();

	void create(PumaTaskEntity pumaTask);

	void update(PumaTaskEntity pumaTask);

	void remove(String name);

	void remove(int id);
}
