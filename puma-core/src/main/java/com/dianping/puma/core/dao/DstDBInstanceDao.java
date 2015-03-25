package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.DstDBInstance;

import java.util.List;

public interface DstDBInstanceDao {

	DstDBInstance find(String name);

	List<DstDBInstance> findAll();

	void create(DstDBInstance entity);

	void update(DstDBInstance entity);

	void remove(String id);
}
