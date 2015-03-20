package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.SyncTask;

import java.util.List;

public interface SyncTaskDao {

	SyncTask find(String name);

	List<SyncTask> findBySyncServerName(String syncServerName);

	List<SyncTask> findAll();

	void create(SyncTask syncTask);

	void remove(String name);
	
	List<SyncTask> find(int offset, int limit);
}
