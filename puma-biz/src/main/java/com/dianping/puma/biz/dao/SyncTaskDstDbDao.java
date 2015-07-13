package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SyncTaskDstDbEntity;

public interface SyncTaskDstDbDao {

	SyncTaskDstDbEntity findByTaskId(String taskId);

	int insert(SyncTaskDstDbEntity entity);

	int update(SyncTaskDstDbEntity entity);

	int delete(SyncTaskDstDbEntity entity);
}
