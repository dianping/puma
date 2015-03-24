package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.SrcDBInstance;

import java.util.List;

public interface SrcDBInstanceDao {

	SrcDBInstance find(String name);

	List<SrcDBInstance> findAll();

	void create(SrcDBInstance srcDBInstance);

	void update(SrcDBInstance srcDBInstance);

	void remove(String name);
}
