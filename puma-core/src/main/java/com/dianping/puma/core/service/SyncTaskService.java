package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.SyncTask;

import java.util.List;

public interface SyncTaskService {

	SyncTask find(String name);

	List<SyncTask> findBySyncServerName(String syncServerName);

	List<SyncTask> findAll();

	void create(SyncTask syncTask);
}
