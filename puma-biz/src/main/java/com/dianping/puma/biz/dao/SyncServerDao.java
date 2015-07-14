package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SyncServerEntity;

public interface SyncServerDao {

	SyncServerEntity find(int id);

	int insert(SyncServerEntity entity);

	int update(SyncServerEntity entity);

	int delete(SyncServerEntity entity);
}
