package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SyncTaskMappingEntity;

public interface SyncTaskMappingDao {

	SyncTaskMappingEntity findByTaskId(String taskId);

	int insert(SyncTaskMappingEntity entity);

	int update(SyncTaskMappingEntity entity);

	int delete(SyncTaskMappingEntity entity);
}
