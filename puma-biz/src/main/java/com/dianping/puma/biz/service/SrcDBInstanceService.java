package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.old.SrcDBInstance;

import java.util.List;

public interface SrcDBInstanceService {
	
	SrcDBInstance find(long id);

	SrcDBInstance find(String name);

    List<SrcDBInstance> findByIp(String ip);

    List<SrcDBInstance> findAll();

    long count();

	List<SrcDBInstance> findByPage(int page, int pageSize);
	
	void create(SrcDBInstance srcDBInstance);

	void update(SrcDBInstance srcDBInstance);

	void remove(String name);
	
	void remove(long id);
}
