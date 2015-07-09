package com.dianping.puma.biz.olddao;

import com.dianping.puma.biz.entity.old.DstDBInstance;

import java.util.List;

public interface DstDBInstanceDao {

	DstDBInstance find(long id);
	
	DstDBInstance find(String name);

	List<DstDBInstance> findAll();

	long count();

	List<DstDBInstance> findByPage(int page, int pageSize);
	
	void create(DstDBInstance entity);

	void update(DstDBInstance entity);

	void remove(String name);
	
	void remove(long id);
}
