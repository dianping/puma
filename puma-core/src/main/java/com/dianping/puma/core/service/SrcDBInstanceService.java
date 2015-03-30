package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.SrcDBInstance;

import java.util.List;

public interface SrcDBInstanceService {
	
	SrcDBInstance find(long id);

	SrcDBInstance find(String name);

    List<SrcDBInstance> findByIp(String ip);

    List<SrcDBInstance> findAll();

	void create(SrcDBInstance srcDBInstance);

	void update(SrcDBInstance srcDBInstance);

	void remove(String name);
	
	void remove(long id);
}
