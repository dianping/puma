package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.PumaTask;

import java.util.List;

public interface PumaTaskService {
	
	PumaTask find(long id);

	PumaTask find(String name);

	List<PumaTask> findBySrcDBInstanceName(String srcDBInstanceName);

	List<PumaTask> findByPumaServerName(String pumaServerName);
	
	List<PumaTask> findByPumaServerNames(String pumaServerName);

	List<PumaTask> findAll();

	long count();

	List<PumaTask> findByPage(int page, int pageSize);
	
	void create(PumaTask pumaTask);

	void update(PumaTask pumaTask);

	void remove(String name);
	
	void remove(long id);
}
