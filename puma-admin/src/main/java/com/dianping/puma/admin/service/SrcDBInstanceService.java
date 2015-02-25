package com.dianping.puma.admin.service;

import com.dianping.puma.core.entity.SrcDBInstanceEntity;

import java.util.List;

public interface SrcDBInstanceService {

	SrcDBInstanceEntity find(String id);

	List<SrcDBInstanceEntity> findAll();

	void create(SrcDBInstanceEntity entity);

	void update(SrcDBInstanceEntity entity);

	void remove(String id);
}
