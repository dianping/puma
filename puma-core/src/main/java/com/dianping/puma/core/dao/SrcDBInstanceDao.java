package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.SrcDBInstanceEntity;

import java.util.List;

public interface SrcDBInstanceDao {

	SrcDBInstanceEntity find(String id);

	List<SrcDBInstanceEntity> findAll();

	void create(SrcDBInstanceEntity entity);

	void update(SrcDBInstanceEntity entity);

	void remove(String id);
}
