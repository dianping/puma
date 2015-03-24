package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.PumaTask;

import java.util.List;

public interface PumaTaskService {

	PumaTask find(String name);

	List<PumaTask> findBySrcDBInstanceName(String srcDBInstanceName);

	List<PumaTask> findByPumaServerName(String pumaServerName);

	List<PumaTask> findAll();

	void create(PumaTask pumaTask);

	void update(PumaTask pumaTask);

	void remove(String name);
}
