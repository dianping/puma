package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SyncServerEntity;

import java.util.List;

public interface SyncServerDao {

	SyncServerEntity find(int id);

	List<SyncServerEntity> findAll();

	int insert(SyncServerEntity entity);

	int update(SyncServerEntity entity);

	int delete(int id);
}
