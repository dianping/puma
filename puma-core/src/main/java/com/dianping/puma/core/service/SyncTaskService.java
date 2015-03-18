package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.SyncTask;

import java.util.List;

public interface SyncTaskService {

	SyncTask find(String name);

	List<SyncTask> findAll();

	void create(SyncTask syncTask);
}
