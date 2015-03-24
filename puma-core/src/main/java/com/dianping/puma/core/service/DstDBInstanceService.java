package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.DstDBInstance;

import java.util.List;

public interface DstDBInstanceService {

	DstDBInstance find(String name);

	List<DstDBInstance> findAll();

	void create(DstDBInstance dstDBInstance);

	void update(DstDBInstance dstDBInstance);

	void remove(String name);
}
