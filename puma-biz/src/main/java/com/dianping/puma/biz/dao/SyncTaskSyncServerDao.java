package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SyncTaskSyncServerEntity;

public interface SyncTaskSyncServerDao {

	SyncTaskSyncServerEntity findByTaskId(int taskId);

	int insert(SyncTaskSyncServerEntity entity);

	int update(SyncTaskSyncServerEntity entity);

	int delete(SyncTaskSyncServerEntity entity);
}
