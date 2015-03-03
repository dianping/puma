package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.SrcDBInstance;

import java.util.List;

public interface SrcDBInstanceDao {

	SrcDBInstance find(String id);

	SrcDBInstance findByName(String name);

	List<SrcDBInstance> findAll();

	void create(SrcDBInstance entity);

	void update(SrcDBInstance entity);

	void remove(String id);
}
