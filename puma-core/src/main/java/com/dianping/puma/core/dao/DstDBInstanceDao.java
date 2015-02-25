package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.DstDBInstanceEntity;

import java.util.List;

public interface DstDBInstanceDao {

	DstDBInstanceEntity find(String id);

	List<DstDBInstanceEntity> findAll();

	void create(DstDBInstanceEntity entity);

	void update(DstDBInstanceEntity entity);

	void remove(String id);
}
