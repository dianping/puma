package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SyncTaskEntity;

public interface SyncTaskDao {

	SyncTaskEntity find(int id);

	int insert(SyncTaskEntity entity);

	int update(SyncTaskEntity entity);

	int delete(SyncTaskEntity entity);
}
