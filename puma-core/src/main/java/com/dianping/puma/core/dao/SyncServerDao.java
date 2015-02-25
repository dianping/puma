package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.SyncServerEntity;

import java.util.List;

public interface SyncServerDao {

	SyncServerEntity find(String id);

	List<SyncServerEntity> findAll();

	void create(SyncServerEntity entity);

	void update(SyncServerEntity entity);

	void remove(String id);
}
