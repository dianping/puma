package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.old.DstDBInstance;

import java.util.List;

public interface DstDBInstanceService {
	
	DstDBInstance find(long id);

	DstDBInstance find(String name);

	List<DstDBInstance> findAll();

	long count();

	List<DstDBInstance> findByPage(int page, int pageSize);
	
	void create(DstDBInstance dstDBInstance);

	void update(DstDBInstance dstDBInstance);

	void remove(String name);
	
	void remove(long id);
}
