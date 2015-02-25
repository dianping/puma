package com.dianping.puma.admin.service;

import com.dianping.puma.core.entity.DstDBInstanceEntity;

import java.util.List;

public interface DstDBInstanceService {

	DstDBInstanceEntity find(String id);

	List<DstDBInstanceEntity> findAll();

	void create(DstDBInstanceEntity entity);

	void update(DstDBInstanceEntity entity);

	void remove(String id);
}
