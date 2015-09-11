package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.SyncTaskEntity;

import java.util.List;

public interface SyncTaskService {
	
	SyncTaskEntity findById(int id);

	SyncTaskEntity findByName(String name);

	List<SyncTaskEntity> findByServerName(String serverName);
	
	List<SyncTaskEntity> findAll();

	int create(SyncTaskEntity entity);
	
	int update(SyncTaskEntity entity);

	int remove(int id);

	int remove(String name);
}
