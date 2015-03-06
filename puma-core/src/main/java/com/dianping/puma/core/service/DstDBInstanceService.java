package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.DstDBInstance;

import java.util.List;

public interface DstDBInstanceService {

	DstDBInstance find(String id);

	DstDBInstance findByName(String name);

	List<DstDBInstance> findAll();

	void create(DstDBInstance entity);

	void update(DstDBInstance entity);

	void remove(String id);
}
